package com.xenikii.timecalculator.features.landing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListState
import com.xenikii.timecalculator.features.routineslist.presentation.sortRoutines
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Instant

class LandingViewModel(
    val routineRepository: RoutineRepository,
    private val routineScheduleRepository: RoutineScheduleRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<LandingState>(LandingState.Loading)
    val viewState: StateFlow<LandingState> = _viewState

    private val _now = MutableStateFlow(Instant.fromEpochMilliseconds(System.currentTimeMillis()))

    init {
        startTimer()
        loadRoutines()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _now.value = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            }
        }
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            combine(routineRepository.routinesFlow, _now) { routines, now ->
                Pair(routines, now)
            }.collect { (routines, now) ->
                val sorted = sortRoutines(
                    routines = routines,
                    now = now,
                    routineScheduleRepository = routineScheduleRepository,
                    sort = RoutinesListState.Sort(
                        RoutinesListState.Sort.SortType.DATE,
                        RoutinesListState.Sort.SortOrder.DESCENDING
                    )
                )
                val routineStates = sorted.take(3).map { item ->
                    val schedule = item.schedule
                    val taskCount = schedule.tasks.size
                    val isOngoing = item.cardViewItem.isOngoing
                    val currentIndex = when {
                        taskCount == 0 -> null
                        isOngoing -> schedule.taskIndexAt(now)
                        else -> 0
                    }
                    val taskViewItems = schedule.tasks.map { task ->
                        createLandingCardTaskViewItem(task = task, now = now)
                    }
                    val validCurrentIndex = currentIndex?.takeIf { it in taskViewItems.indices }
                    val taskDistribution = distributeTasks(
                        taskViewItems = taskViewItems,
                        isRoutineOngoing = isOngoing,
                        currentTaskIndex = validCurrentIndex,
                    )
                    LandingRoutineState(
                        routineId = item.routine.id,
                        cardViewItem = item.cardViewItem,
                        completedTasks = taskDistribution.completedTasks,
                        previewTasks = taskDistribution.previewTasks,
                        futureTasks = taskDistribution.futureTasks,
                        hasHiddenTasks = taskDistribution.hasHiddenTasks,
                    )
                }
                _viewState.value = LandingState.Success(routineStates = routineStates)
            }
        }
    }
}

private data class LandingTaskDistribution(
    val completedTasks: List<LandingCardTaskViewItem>,
    val previewTasks: List<LandingCardTaskViewItem>,
    val futureTasks: List<LandingCardTaskViewItem>,
    val hasHiddenTasks: Boolean,
)

private fun distributeTasks(
    taskViewItems: List<LandingCardTaskViewItem>,
    isRoutineOngoing: Boolean,
    currentTaskIndex: Int?,
): LandingTaskDistribution {
    val completedTasks = if (isRoutineOngoing && currentTaskIndex != null) {
        taskViewItems.take(currentTaskIndex)
    } else {
        emptyList()
    }
    val upcomingTasks = if (isRoutineOngoing && currentTaskIndex != null) {
        taskViewItems.drop(currentTaskIndex)
    } else {
        taskViewItems
    }
    val previewTasks = upcomingTasks.take(2)
    val futureTasks = upcomingTasks.drop(2)

    return LandingTaskDistribution(
        completedTasks = completedTasks,
        previewTasks = previewTasks,
        futureTasks = futureTasks,
        hasHiddenTasks = completedTasks.isNotEmpty() || futureTasks.isNotEmpty(),
    )
}

sealed interface LandingState {
    object Loading : LandingState
    data class Success(
        val routineStates: List<LandingRoutineState>
    ) : LandingState

    data class Error(val error: String) : LandingState
}
