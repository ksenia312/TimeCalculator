package com.example.morningcalculator.features.taskeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.model.RoutineLink
import com.example.morningcalculator.domain.model.TaskRequest
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class CreateTaskViewModel(
    private val routineId: String?,
    private val tasksRepository: TasksRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    val hasRoutine = routineId != null

    private val _showDuplicateError = MutableStateFlow(false)
    val showDuplicateError: StateFlow<Boolean> = _showDuplicateError.asStateFlow()

    fun createTask(request: TaskRequest, selectedDurationIndex: Int?): Boolean {
        if (request.durations.hasDuplicateDurations()) {
            _showDuplicateError.value = true
            return false
        }
        _showDuplicateError.value = false
        viewModelScope.launch {
            val task = tasksRepository.addTask(request)
            val scopedRoutineId = routineId ?: return@launch
            val routine = routineRepository.getRoutineFlow(scopedRoutineId).first() ?: return@launch
            val subData = task.data.getOrNull(selectedDurationIndex ?: 0) ?: task.data.firstOrNull() ?: return@launch
            routineRepository.updateRoutine(
                routine.copy(
                    data = routine.data + RoutineLink(
                        id = UUID.randomUUID().toString(),
                        task = task,
                        subData = subData,
                    )
                )
            )
        }
        return true
    }
}
