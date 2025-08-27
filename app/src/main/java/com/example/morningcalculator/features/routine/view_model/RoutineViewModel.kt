package com.example.morningcalculator.features.routine.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineFullLink
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.model.TaskUpdateRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.core.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RoutineViewModel(
    val id: String, val tasksRepository: TasksRepository, val routineRepository: RoutineRepository
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
                    id = UUID.randomUUID().toString(), task = task, subData = subData
                )
            )
        }
    }

//    fun editTask(request: TaskUpdateRequest, selectedDurationIndex: Int) {
//        viewModelScope.launch {
//            val task = tasksRepository.updateTask(request)
//            val subData = task.data[selectedDurationIndex]
//            addOrEditTaskInRoutine(
//                RoutineFullLink(
//                    id = request.taskId, task = task, subData = subData
//                )
//            )
//        }
//    }

    fun deleteTask(linkId: String) {
        viewModelScope.launch {
            _viewState.asSuccess {
                val routineCombined = it.full
                val newRoutineCombined = routineCombined.copy(
                    data = routineCombined.data.mapNotNull { e ->
                        if (e.id == linkId) null else e
                    })

                editRoutine(newRoutineCombined.toLinks())
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
                editRoutine(newRoutine.toLinks())
            }
        }
    }

    fun addOrEditTasksInRoutine(tasks: List<Pair<Int, Task>>) {
        viewModelScope.launch {

        }
    }

    fun addOrEditTaskInRoutine(entryFullData: RoutineFullLink) {
        viewModelScope.launch {
            _viewState.asSuccess { r ->
                val routineCombined = r.full
                var newRoutineCombined = routineCombined.copy(
                    data = routineCombined.data.map { entry ->
                        if (entry.id == entryFullData.id) entry.copy(
                            subData = entryFullData.subData, task = entryFullData.task
                        ) else entry
                    })
                if (!newRoutineCombined.data.map { it.id }.contains(entryFullData.id)) {
                    newRoutineCombined = newRoutineCombined.copy(
                        data = newRoutineCombined.data + entryFullData
                    )
                }
                editRoutine(newRoutineCombined.toLinks())
            }
        }
    }

    fun editRoutine(routine: Routine.Links) {
        viewModelScope.launch {
            routineRepository.updateRoutine(routine)
        }
    }

    fun loadRoutine() {
        routineRepository.initializeId(id)

        viewModelScope.launch {
            tasksRepository.tasksFlow().collect {
                _tasksState.value = it
                val viewState = _viewState.value
                if (viewState is RoutineViewState.Success) {
                    _viewState.value = RoutineViewState.Success(
                        viewState.links.toFull(), viewState.links
                    )
                }
            }
        }

        viewModelScope.launch {
            routineRepository.routineFlow().collect { routine ->
                _viewState.value = if (routine == null) RoutineViewState.Error("Routine not found")
                else RoutineViewState.Success(routine.toFull(), routine)
            }
        }
    }

    fun Routine.Links.toFull(): Routine.Full {
        val routineTasks = links.mapNotNull { entry ->
            val task = _tasksState.value.firstOrNull { task -> task.id == entry.taskId }
            val subData = task?.data?.firstOrNull { subData -> subData.id == entry.subDataId }
            if (task != null && subData != null) RoutineFullLink(
                id = entry.id, task = task, subData = subData
            )
            else null
        }
        return Routine.Full(
            id = id,
            title = title,
            data = routineTasks,
            time = time,
            modifiedAt = modifiedAt,
            color = color
        )

    }

    private fun Routine.Full.toLinks(): Routine.Links {
        val entries = data.map { entry ->
            RoutineLink(
                id = entry.id, taskId = entry.task.id, subDataId = entry.subData.id
            )
        }
        return Routine.Links(
            id = id,
            title = title,
            links = entries,
            time = time,
            modifiedAt = modifiedAt,
            color = color
        )
    }
}

fun MutableStateFlow<RoutineViewState>.asSuccess(action: (RoutineViewState.Success) -> Unit): Unit {
    (this.value as? RoutineViewState.Success)?.let {
        action(it)
    }
}


sealed interface RoutineViewState {
    object Loading : RoutineViewState
    data class Success(val full: Routine.Full, val links: Routine.Links) : RoutineViewState

    data class Error(val error: String) : RoutineViewState
}