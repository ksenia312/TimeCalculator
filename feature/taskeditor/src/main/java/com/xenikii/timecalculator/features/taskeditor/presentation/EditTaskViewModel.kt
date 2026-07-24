package com.xenikii.timecalculator.features.taskeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.domain.model.TaskUpdateRequest
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.TasksRepository
import com.xenikii.timecalculator.shared.navigator.EditTaskArguments
import com.xenikii.timecalculator.shared.navigator.EditTaskSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration

class EditTaskViewModel(
    arguments: EditTaskArguments,
    private val tasksRepository: TasksRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<EditTaskViewState>(EditTaskViewState.Loading)
    val viewState: StateFlow<EditTaskViewState> = _viewState.asStateFlow()

    private val source = arguments.source
    private val taskId: String = arguments.taskId

    val hasRoutine: Boolean = source is EditTaskSource.Routine

    private val _showDuplicateError = MutableStateFlow(false)
    val showDuplicateError: StateFlow<Boolean> = _showDuplicateError.asStateFlow()

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
        durations: List<Duration>,
        selectedDurationIndex: Int?,
    ): Boolean {
        if (durations.hasDuplicateDurations()) {
            _showDuplicateError.value = true
            return false
        }
        _showDuplicateError.value = false
        viewModelScope.launch {
            val currentTask = (_viewState.value as? EditTaskViewState.Success)?.task
            val updatedTask = tasksRepository.updateTask(
                TaskUpdateRequest(
                    taskId = taskId,
                    title = title,
                    description = currentTask?.description.orEmpty(),
                    durations = durations,
                )
            )
            if (hasRoutine) {
                updateRoutineLinkWithTask(updatedTask, selectedDurationIndex)
            }
        }
        return true
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
