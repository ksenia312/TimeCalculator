package com.xenikii.timecalculator.features.taskeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.TaskRequest
import com.xenikii.timecalculator.domain.repository.FREE_TASK_LIMIT
import com.xenikii.timecalculator.domain.repository.PremiumRepository
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.TasksRepository
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
    private val premiumRepository: PremiumRepository,
) : ViewModel() {

    val hasRoutine = routineId != null

    private val _showDuplicateError = MutableStateFlow(false)
    val showDuplicateError: StateFlow<Boolean> = _showDuplicateError.asStateFlow()

    // The free-tier task limit can only be checked with suspend calls (SDK entitlement lookup +
    // a DB count), so unlike the duplicate-durations check above this can't decide synchronously
    // whether to navigate back - both outcomes are reported through onSaved/onPremiumRequired
    // once the coroutine resolves, instead of a return value.
    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    fun onPaywallShown() {
        _showPaywall.value = false
    }

    fun createTask(
        request: TaskRequest,
        selectedDurationIndex: Int?,
        onSaved: () -> Unit,
    ) {
        if (request.durations.hasDuplicateDurations()) {
            _showDuplicateError.value = true
            return
        }
        _showDuplicateError.value = false
        viewModelScope.launch {
            val isPremium = premiumRepository.isPremiumNow()
            if (!isPremium && tasksRepository.getTaskCount() >= FREE_TASK_LIMIT) {
                _showPaywall.value = true
                return@launch
            }

            val task = tasksRepository.addTask(request)
            val scopedRoutineId = routineId
            if (scopedRoutineId != null) {
                val routine = routineRepository.getRoutineFlow(scopedRoutineId).first()
                val subData = task.data.getOrNull(selectedDurationIndex ?: 0) ?: task.data.firstOrNull()
                if (routine != null && subData != null) {
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
            onSaved()
        }
    }
}
