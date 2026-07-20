package com.example.morningcalculator.features.routine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.model.TaskUpdateRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.core.repository.TasksRepository
import com.example.morningcalculator.features.home.ui.components.RoutineDialogViewState
import com.example.morningcalculator.features.home.ui.components.toScheduledAtInstant
import com.example.morningcalculator.features.home.ui.components.toRoutineDialogViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RoutineViewModel(
    val id: String,
    val tasksRepository: TasksRepository,
    val routineRepository: RoutineRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow<RoutineViewState>(RoutineViewState.Loading)
    private val _tasksState = MutableStateFlow<List<Task>>(emptyList())

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
        loadRoutine()
    }

    fun addNewTask(request: TaskRequest, selectedDurationIndex: Int) {
        viewModelScope.launch {
            val task = tasksRepository.addTask(request)
            val subData = task.data[selectedDurationIndex]
            addOrEditTaskInRoutine(
                RoutineLink(
                    id = UUID.randomUUID().toString(),
                    task = task,
                    subData = subData
                )
            )
        }
    }

    fun editTask(
        request: TaskUpdateRequest,
        selectedDurationIndex: Int,
        linkId: String
    ) {
        viewModelScope.launch {
            val task = tasksRepository.updateTask(request)
            val subData = task.data[selectedDurationIndex]
            addOrEditTaskInRoutine(
                RoutineLink(
                    id = linkId,
                    task = task,
                    subData = subData
                )
            )
        }
    }

    fun deleteTask(linkId: String) {
        viewModelScope.launch {
            _viewState.asSuccess {
                val routineCombined = it.routine
                val newRoutineCombined = routineCombined.copy(
                    data = routineCombined.data.mapNotNull { e ->
                        if (e.id == linkId) null else e
                    }
                )

                editRoutine(newRoutineCombined)
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

    fun onShowEditDialog() {
        _viewState.asSuccess { state ->
            _viewState.value = state.copy(
                routineDialogViewState = state.routine.toRoutineDialogViewState()
            )
        }
    }

    fun onRoutineDialogViewStateChange(routineDialogViewState: RoutineDialogViewState) {
        _viewState.asSuccess { state ->
            _viewState.value = state.copy(
                routineDialogViewState = routineDialogViewState
            )
        }
    }

    fun onRoutineDialogDismiss() {
        _viewState.asSuccess { state ->
            _viewState.value = state.copy(
                routineDialogViewState = null
            )
        }
    }

    fun onRoutineDialogConfirm() {
        _viewState.asSuccess { state ->
            val dialogState = state.routineDialogViewState ?: return@asSuccess
            editRoutine(
                state.routine.copy(
                    title = dialogState.title,
                    scheduledAt = dialogState.toScheduledAtInstant(),
                    scheduledAtAnchor = dialogState.anchor
                )
            )
            _viewState.value = state.copy(
                routineDialogViewState = null
            )
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
            routineRepository.getRoutineFlow(id)
                .dropWhile { it == null }
                .collect { routine ->
                    val currentDialogState = (_viewState.value as? RoutineViewState.Success)
                        ?.routineDialogViewState
                    _viewState.value =
                        if (routine == null) RoutineViewState.Error
                        else RoutineViewState.Success(
                            routine = routine,
                            routineDialogViewState = currentDialogState
                        )
                }
        }
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
        val routineDialogViewState: RoutineDialogViewState? = null,
    ) : RoutineViewState

    object Error : RoutineViewState
}