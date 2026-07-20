package com.example.morningcalculator.features.routine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineLink
import com.example.morningcalculator.domain.model.Task
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.TasksRepository
import com.example.morningcalculator.shared.extensions.startAtInstant
import com.example.morningcalculator.shared.viewitem.RoutineCardViewItem
import com.example.morningcalculator.shared.viewitem.toRoutineCardViewItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Instant

class RoutineViewModel(
    val id: String,
    val tasksRepository: TasksRepository,
    val routineRepository: RoutineRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow<RoutineViewState>(RoutineViewState.Loading)
    private val _tasksState = MutableStateFlow<List<Task>>(emptyList())
    private val _now = MutableStateFlow(Instant.fromEpochMilliseconds(System.currentTimeMillis()))

    val viewState: StateFlow<RoutineViewState> = _viewState
    val tasks: StateFlow<List<Task>> = combine(
        _tasksState, _viewState
    ) { tasks, _ ->
        tasks
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        startTimer()
        loadRoutine()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _now.value = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            }
        }
    }

    fun reorderTasks(newLinksIds: List<String>) {
        viewModelScope.launch {
            _viewState.asSuccess {
                val fullRoutine = it.routine
                val newTaskPairs = newLinksIds.mapNotNull { id ->
                    fullRoutine.data.firstOrNull { r -> r.id == id }
                }
                val newRoutine = fullRoutine.copy(data = newTaskPairs)
                editRoutine(newRoutine)
            }
        }
    }

    fun editLinksInRoutine(links: List<RoutineLink>) {
        viewModelScope.launch {
            _viewState.asSuccess { r ->
                val modifiedFull = r.routine.copy(data = links)
                editRoutine(modifiedFull)
            }
        }
    }

    fun addOrEditTaskInRoutine(link: RoutineLink) {
        viewModelScope.launch {
            _viewState.asSuccess { r ->
                val routineCombined = r.routine
                var newRoutineCombined = routineCombined.copy(
                    data = routineCombined.data.map { entry ->
                        if (entry.id == link.id) {
                            entry.copy(
                                subData = link.subData,
                                task = link.task
                            )
                        } else {
                            entry
                        }
                    }
                )
                if (!newRoutineCombined.data.map { it.id }.contains(link.id)) {
                    newRoutineCombined = newRoutineCombined.copy(
                        data = newRoutineCombined.data + link
                    )
                }
                editRoutine(newRoutineCombined)
            }
        }
    }

    fun editRoutine(routine: Routine) {
        viewModelScope.launch {
            routineRepository.updateRoutine(routine)
        }
    }

    fun loadRoutine() {
        viewModelScope.launch {
            tasksRepository.tasksFlow.collect {
                _tasksState.value = it
                val viewState = _viewState.value
                if (viewState is RoutineViewState.Success) {
                    _viewState.value = viewState.copy(routine = viewState.routine)
                }
            }
        }

        viewModelScope.launch {
            combine(
                routineRepository.getRoutineFlow(id).dropWhile { it == null },
                _now
            ) { routine, now -> Pair(routine, now) }
            .collect { (routine, now) ->
                _viewState.value =
                    if (routine == null) RoutineViewState.Error
                    else {
                        val cardViewItem = routine.toRoutineCardViewItem(now)
                        RoutineViewState.Success(
                            routine = routine,
                            cardViewItem = cardViewItem,
                            currentTaskIndex = if (cardViewItem.isOngoing) currentTaskIndex(routine, now) else null,
                        )
                    }
            }
        }
    }
}

private fun currentTaskIndex(
    routine: Routine,
    now: Instant,
): Int? {
    val tasks = routine.data
    if (tasks.isEmpty()) return null

    if (now <= routine.startAtInstant()) return 0

    var cursor = routine.startAtInstant()

    tasks.forEachIndexed { index, link ->
        val d = linkDuration(link).coerceAtLeast(Duration.ZERO)
        val next = cursor + d

        if (d == Duration.ZERO) {
            if (now == cursor) return index
        } else {
            if (now < next) return index
        }

        cursor = next
    }

    return tasks.lastIndex
}

private fun linkDuration(link: RoutineLink): Duration {
    return link.subData?.duration ?: link.task.data.fold(Duration.ZERO) { acc, subData ->
        acc + subData.duration
    }
}

fun MutableStateFlow<RoutineViewState>.asSuccess(
    action: (RoutineViewState.Success) -> Unit
) {
    (this.value as? RoutineViewState.Success)?.let { action(it) }
}

sealed interface RoutineViewState {
    object Loading : RoutineViewState
    data class Success(
        val routine: Routine,
        val cardViewItem: RoutineCardViewItem,
        val currentTaskIndex: Int?,
    ) : RoutineViewState

    object Error : RoutineViewState
}