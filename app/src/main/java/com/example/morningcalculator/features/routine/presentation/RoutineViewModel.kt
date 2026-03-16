package com.example.morningcalculator.features.routine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineFullLink
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.model.TaskUpdateRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.core.repository.TasksRepository
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
                RoutineFullLink(
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
                RoutineFullLink(
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
                val routineCombined = it.full
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
                val fullRoutine = it.full
                val newTaskPairs = newLinksIds.mapNotNull { id ->
                    fullRoutine.data.firstOrNull { r -> r.id == id }
                }
                val newRoutine = fullRoutine.copy(data = newTaskPairs)
                editRoutine(newRoutine)
            }
        }
    }

    fun editLinksInRoutine(links: List<RoutineFullLink>) {
        viewModelScope.launch {
            _viewState.asSuccess { r ->
                val modifiedFull = r.full.copy(data = links)
                editRoutine(modifiedFull)
            }
        }
    }

    fun addOrEditTaskInRoutine(entryFullData: RoutineFullLink) {
        viewModelScope.launch {
            _viewState.asSuccess { r ->
                val routineCombined = r.full
                var newRoutineCombined = routineCombined.copy(
                    data = routineCombined.data.map { entry ->
                        if (entry.id == entryFullData.id) {
                            entry.copy(
                                subData = entryFullData.subData,
                                task = entryFullData.task
                            )
                        } else {
                            entry
                        }
                    }
                )
                if (!newRoutineCombined.data.map { it.id }.contains(entryFullData.id)) {
                    newRoutineCombined = newRoutineCombined.copy(
                        data = newRoutineCombined.data + entryFullData
                    )
                }
                editRoutine(newRoutineCombined)
            }
        }
    }

    fun editRoutine(routine: Routine.Full) {
        viewModelScope.launch {
            routineRepository.updateRoutine(routine)
        }
    }

    fun loadRoutine() {
        routineRepository.initializeId(id)

        viewModelScope.launch {
            tasksRepository.tasksFlow.collect {
                _tasksState.value = it
                val viewState = _viewState.value
                if (viewState is RoutineViewState.Success) {
                    _viewState.value = RoutineViewState.Success(viewState.full)
                }
            }
        }

        viewModelScope.launch {
            routineRepository.routineFlow
                .dropWhile { it == null }
                .collect { routine ->
                    _viewState.value =
                        if (routine == null) RoutineViewState.Error("Routine not found")
                        else RoutineViewState.Success(routine)
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
    data class Success(val full: Routine.Full) : RoutineViewState
    data class Error(val error: String) : RoutineViewState
}