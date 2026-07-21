package com.example.morningcalculator.features.routine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineLink
import com.example.morningcalculator.domain.model.RoutineSchedule
import com.example.morningcalculator.domain.model.Task
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.RoutineScheduleRepository
import com.example.morningcalculator.domain.repository.TasksRepository
import com.example.morningcalculator.shared.viewitem.RoutineCardViewItem
import com.example.morningcalculator.shared.viewitem.toViewItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Instant

class RoutineViewModel(
    val id: String,
    val tasksRepository: TasksRepository,
    val routineRepository: RoutineRepository,
    private val routineScheduleRepository: RoutineScheduleRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<RoutineViewState>(RoutineViewState.Loading)
    private val _tasksState = MutableStateFlow<List<Task>>(emptyList())
    private val _now = MutableStateFlow(Instant.fromEpochMilliseconds(System.currentTimeMillis()))
    private val _draftOrder = MutableStateFlow<List<String>?>(null)

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

    fun previewReorder(from: Int, to: Int) {
        val base = _draftOrder.value
            ?: (_viewState.value as? RoutineViewState.Success)?.routine?.data?.map { it.id }
            ?: return
        if (from !in base.indices || to !in base.indices) return
        _draftOrder.value = base.toMutableList().apply { move(from, to) }
    }

    fun commitReorder() {
        val draft = _draftOrder.value ?: return
        val current = (_viewState.value as? RoutineViewState.Success)?.routine ?: return
        val ordered = current.applyOrder(draft)
        viewModelScope.launch {
            routineRepository.updateRoutine(ordered)
        }
    }

    fun cancelReorder() {
        _draftOrder.value = null
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
            }
        }

        viewModelScope.launch {
            combine(
                routineRepository.getRoutineFlow(id).dropWhile { it == null },
                _now,
                _draftOrder,
            ) { routine, now, draftOrder -> Triple(routine, now, draftOrder) }
                .collect { (routine, now, draftOrder) ->
                    if (routine == null) {
                        _viewState.value = RoutineViewState.Error
                        return@collect
                    }

                    if (draftOrder != null && routine.data.map { it.id } == draftOrder) {
                        _draftOrder.value = null
                        return@collect
                    }

                    val ordered = draftOrder?.let { routine.applyOrder(it) } ?: routine
                    val schedule = routineScheduleRepository.computeSchedule(ordered, now)
                    val cardViewItem = ordered.toViewItem(schedule, now)
                    _viewState.value = RoutineViewState.Success(
                        routine = ordered,
                        schedule = schedule,
                        cardViewItem = cardViewItem,
                        currentTaskIndex = schedule.taskIndexAt(now),
                    )
                }
        }
    }
}

private fun Routine.applyOrder(order: List<String>): Routine {
    val byId = data.associateBy { it.id }
    val orderSet = order.toSet()
    val reordered = order.mapNotNull { byId[it] }
    val leftovers = data.filter { it.id !in orderSet }
    return copy(data = reordered + leftovers)
}

private fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) return
    val element = removeAt(fromIndex)
    add(toIndex, element)
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
        val schedule: RoutineSchedule,
        val cardViewItem: RoutineCardViewItem,
        val currentTaskIndex: Int?,
    ) : RoutineViewState

    object Error : RoutineViewState
}