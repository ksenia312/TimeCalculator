package com.example.morningcalculator.features.routine.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineCombined
import com.example.morningcalculator.core.model.RoutineEntry
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
import kotlin.time.Duration

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
            is RoutineViewState.Success -> {
                val routineCombined = toCombined(state.routine)
                routineCombined.taskPairs.map { it.first.id }.toSet()
            }

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

    fun reorderTasks(newTaskIds: List<String>) {
        viewModelScope.launch {
            val routineCombined = toCombined((_viewState.value as RoutineViewState.Success).routine)
            val newTaskPairs = newTaskIds.mapNotNull { taskId ->
                routineCombined.taskPairs.firstOrNull { it.first.id == taskId }
            }
            val newRoutine = routineCombined.copy(taskPairs = newTaskPairs)
            editRoutine(newRoutine.toRoutine())
        }
    }

    fun addOrEditTaskInRoutine(task: Task, subData: SubData) {
        viewModelScope.launch {
            val routineCombined = toCombined((_viewState.value as RoutineViewState.Success).routine)

            var newRoutineCombined = routineCombined.copy(
                taskPairs = routineCombined.taskPairs.map { (t, s) ->
                    if (t.id == task.id) (task to subData) else t to s
                })
            if (!newRoutineCombined.taskPairs.map { it.first.id }.contains(task.id)) {
                newRoutineCombined = newRoutineCombined.copy(
                    taskPairs = newRoutineCombined.taskPairs + (task to subData)
                )
            }
            editRoutine(newRoutineCombined.toRoutine())
        }
    }

    fun editRoutine(routine: Routine) {
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
                        viewState.routine
                    )
                }
            }
        }

        viewModelScope.launch {
            routineRepository.routineFlow().collect { routine ->
                _viewState.value = if (routine == null) RoutineViewState.Error("Routine not found")
                else RoutineViewState.Success(routine)
            }
        }
    }

    fun toCombined(routine: Routine): RoutineCombined {
        val routineTasks = routine.entries.mapNotNull { entry ->
            val task = _tasksState.value.firstOrNull { task -> task.id == entry.taskId }
            val subData = task?.data?.firstOrNull { subData -> subData.id == entry.subDataId }
            if (task != null && subData != null) task to subData else null
        }
        return routine.run {
            RoutineCombined(
                routineId = id,
                title = title,
                taskPairs = routineTasks,
                time = time
            )
        }
    }

    private fun RoutineCombined.toRoutine(): Routine {
        val entries = taskPairs.map { (task, subData) ->
            RoutineEntry(taskId = task.id, subDataId = subData.id)
        }
        return Routine(id = routineId, title = title, entries = entries, time = time)
    }
}


sealed interface RoutineViewState {
    object Loading : RoutineViewState
    data class Success(/*val combined: RoutineCombined,*/ val routine: Routine) : RoutineViewState
    data class Error(val error: String) : RoutineViewState
}