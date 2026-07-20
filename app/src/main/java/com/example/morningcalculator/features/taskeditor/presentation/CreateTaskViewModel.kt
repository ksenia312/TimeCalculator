package com.example.morningcalculator.features.taskeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.core.repository.TasksRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class CreateTaskViewModel(
    private val routineId: String?,
    private val tasksRepository: TasksRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    fun createTask(request: TaskRequest, selectedDurationIndex: Int?) {
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
    }
}
