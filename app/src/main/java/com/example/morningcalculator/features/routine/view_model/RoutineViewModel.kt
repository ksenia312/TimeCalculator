package com.example.morningcalculator.features.routine.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.SubData
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

class RoutineViewModel(
    val id: String, val tasksRepository: TasksRepository, val routineRepository: RoutineRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow<RoutineViewState>(RoutineViewState.Loading)
    private val _tasksState = MutableStateFlow<List<Task>>(emptyList())

    val viewState: StateFlow<RoutineViewState> = _viewState
    val notIncludedTasks: StateFlow<List<Task>> = combine(
        _tasksState, _viewState
    ) { tasks, state ->

        val idsInRoutine: Set<String> = when (state) {
            is RoutineViewState.Success -> state.full.data.map { it.first.id }.toSet()

            else -> emptySet()
        }
        tasks.filter { it.id !in idsInRoutine }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        loadRoutine()
    }

    fun addNewTask(request: TaskRequest, selectedDurationIndex: Int) {
        viewModelScope.launch {
            val task = tasksRepository.addTask(request)
            addOrEditTaskInRoutine(task, task.data[selectedDurationIndex])
        }
    }

    fun editTask(request: TaskUpdateRequest, selectedDurationIndex: Int) {
        viewModelScope.launch {
            val task = tasksRepository.updateTask(request)
            addOrEditTaskInRoutine(task, task.data[selectedDurationIndex])
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _viewState.asSuccess {
                val routineCombined = it.full
                val newRoutineCombined = routineCombined.copy(
                    data = routineCombined.data.mapNotNull { (t, s) ->
                        if (t.id == taskId) null else t to s
                    })

                editRoutine(newRoutineCombined.toLinks())
            }

        }
    }

    fun reorderTasks(newTaskIds: List<String>) {
        viewModelScope.launch {
            _viewState.asSuccess {
                val routineCombined = it.full
                val newTaskPairs = newTaskIds.mapNotNull { taskId ->
                    routineCombined.data.firstOrNull { it.first.id == taskId }
                }
                val newRoutine = routineCombined.copy(data = newTaskPairs)
                editRoutine(newRoutine.toLinks())
            }
        }
    }

    fun addOrEditTaskInRoutine(task: Task, subData: SubData) {
        viewModelScope.launch {
            _viewState.asSuccess {
                val routineCombined = it.full
                var newRoutineCombined = routineCombined.copy(
                    data = routineCombined.data.map { (t, s) ->
                        if (t.id == task.id) (task to subData) else t to s
                    })
                if (!newRoutineCombined.data.map { it.first.id }.contains(task.id)) {
                    newRoutineCombined = newRoutineCombined.copy(
                        data = newRoutineCombined.data + (task to subData)
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
            if (task != null && subData != null) task to subData else null
        }
        return Routine.Full(
            id = id, title = title, data = routineTasks, time = time, modifiedAt = modifiedAt
        )

    }

    private fun Routine.Full.toLinks(): Routine.Links {
        val entries = data.map { (task, subData) ->
            RoutineLink(taskId = task.id, subDataId = subData.id)
        }
        return Routine.Links(
            id = id, title = title, links = entries, time = time, modifiedAt = modifiedAt
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