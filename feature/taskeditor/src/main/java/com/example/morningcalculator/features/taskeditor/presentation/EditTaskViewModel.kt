package com.example.morningcalculator.features.taskeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.model.RoutineLink
import com.example.morningcalculator.domain.model.SubData
import com.example.morningcalculator.domain.model.Task
import com.example.morningcalculator.domain.model.TaskUpdateRequest
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.TasksRepository
import com.example.morningcalculator.shared.navigator.EditTaskArguments
import com.example.morningcalculator.shared.navigator.EditTaskSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditTaskViewModel(
    private val arguments: EditTaskArguments,
    private val tasksRepository: TasksRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<EditTaskViewState>(EditTaskViewState.Loading)
    val viewState: StateFlow<EditTaskViewState> = _viewState.asStateFlow()
    private val source = arguments.source
    private val taskId: String = arguments.taskId

    val hasRoutine: Boolean = source is EditTaskSource.Routine

    init {
        viewModelScope.launch {
            tasksRepository.getTaskFlow(taskId).collect { task ->
                _viewState.value = if (task == null) {
                    EditTaskViewState.Error
                } else {
                    val selectedIndex = if (hasRoutine) {
                        val selectedSubDataId = (source as EditTaskSource.Routine).selectedSubDataId
                        if (selectedSubDataId == null) {
                            0
                        } else {
                            task.data.indexOfFirst { it.id == selectedSubDataId }
                                .takeIf { it >= 0 }
                                ?: 0
                        }
                    } else {
                        null
                    }
                    EditTaskViewState.Success(task = task, initialSelectedIndex = selectedIndex)
                }
            }
        }
    }

    fun saveTask(
        title: String,
        subData: List<SubData>,
        selectedDurationIndex: Int?,
    ) {
        viewModelScope.launch {
            val currentTask = (_viewState.value as? EditTaskViewState.Success)?.task
            val updatedTask = tasksRepository.updateTask(
                TaskUpdateRequest(
                    taskId = taskId,
                    title = title,
                    description = currentTask?.description.orEmpty(),
                    subData = subData,
                )
            )
            if (hasRoutine) {
                updateRoutineLinkWithTask(updatedTask, selectedDurationIndex)
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            if (hasRoutine) {
                val routineSource = source as EditTaskSource.Routine
                val routine = routineRepository.getRoutineFlow(routineSource.routineId).first() ?: return@launch
                routineRepository.updateRoutine(
                    routine.copy(
                        data = routine.data.filterNot { it.id == routineSource.linkId }
                    )
                )
            } else {
                tasksRepository.deleteTask(taskId)
            }
        }
    }

    private suspend fun updateRoutineLinkWithTask(
        updatedTask: Task,
        selectedDurationIndex: Int?,
    ) {
        val routineSource = source as? EditTaskSource.Routine ?: return
        val routine = routineRepository.getRoutineFlow(routineSource.routineId).first() ?: return
        val selectedSubData = updatedTask.data.getOrNull(selectedDurationIndex ?: 0)
            ?: updatedTask.data.firstOrNull()
            ?: return
        val updatedLinks = routine.data.map { link ->
            if (link.id == routineSource.linkId) {
                RoutineLink(
                    id = link.id,
                    task = updatedTask,
                    subData = selectedSubData,
                )
            } else {
                link
            }
        }
        routineRepository.updateRoutine(routine.copy(data = updatedLinks))
    }
}

sealed interface EditTaskViewState {
    data object Loading : EditTaskViewState
    data class Success(
        val task: Task,
        val initialSelectedIndex: Int?,
    ) : EditTaskViewState

    data object Error : EditTaskViewState
}
