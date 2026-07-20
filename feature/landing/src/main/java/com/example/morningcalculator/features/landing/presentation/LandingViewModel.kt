package com.example.morningcalculator.features.landing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.features.landing.ui.currentTaskIndex
import com.example.morningcalculator.features.routineslist.presentation.RoutinesListState
import com.example.morningcalculator.features.routineslist.presentation.sortRoutines
import com.example.morningcalculator.shared.viewitem.toRoutineCardViewItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Instant

class LandingViewModel(
    val routineRepository: RoutineRepository,
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
                    sort = RoutinesListState.Sort(
                        RoutinesListState.Sort.SortType.DATE,
                        RoutinesListState.Sort.SortOrder.DESCENDING
                    )
                )
                val routineStates = sorted.take(3).map { routine ->
                    val cardViewItem = routine.toRoutineCardViewItem(now)
                    val taskCount = routine.data.size
                    val isOngoing = cardViewItem.isOngoing
                    val currentIndex = when {
                        taskCount == 0 -> null
                        isOngoing -> currentTaskIndex(routine, now)
                        else -> 0
                    }
                    val nextIndex = when {
                        taskCount == 0 -> null
                        isOngoing -> currentIndex?.plus(1)
                        else -> 1
                    }?.takeIf { it in 0 until taskCount }
                    LandingRoutineState(
                        routineId = routine.id,
                        cardViewItem = cardViewItem,
                        currentTaskViewItem = currentIndex?.let { createLandingCardTaskViewItem(routine, it, now) },
                        nextTaskViewItem = nextIndex?.let { createLandingCardTaskViewItem(routine, it, now) },
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
