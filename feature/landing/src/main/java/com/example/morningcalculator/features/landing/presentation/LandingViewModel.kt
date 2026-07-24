package com.example.morningcalculator.features.landing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.RoutineScheduleRepository
import com.example.morningcalculator.features.routineslist.presentation.RoutinesListState
import com.example.morningcalculator.features.routineslist.presentation.sortRoutines
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
                    LandingRoutineState(
                        routineId = item.routine.id,
                        cardViewItem = item.cardViewItem,
                        taskViewItems = schedule.tasks.map { task ->
                            createLandingCardTaskViewItem(task = task, now = now)
                        },
                        currentTaskIndex = currentIndex?.takeIf { it in schedule.tasks.indices },
                    )
                }
                _viewState.value = LandingState.Success(routineStates = routineStates)
            }
        }
    }
}

sealed interface LandingState {
    object Loading : LandingState
    data class Success(
        val routineStates: List<LandingRoutineState>
    ) : LandingState

    data class Error(val error: String) : LandingState
}
