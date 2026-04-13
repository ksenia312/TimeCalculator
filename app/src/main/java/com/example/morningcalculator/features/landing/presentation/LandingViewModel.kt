package com.example.morningcalculator.features.landing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.shared.extensions.isOngoing
import com.example.morningcalculator.shared.extensions.startAtInstant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

class LandingViewModel(
    val routineRepository: RoutineRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<LandingState>(LandingState.Loading)
    val viewState: StateFlow<LandingState> = _viewState

    init {
        loadRoutines()
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            routineRepository.routinesFlow.collect { routines ->
                val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                val ongoingRoutines = routines.filter { it.isOngoing() }
                val displayRoutines = ongoingRoutines.ifEmpty {
                    routines
                        .filter { routine ->
                            routine.startAtInstant() > now
                        }
                        .sortedBy { routine ->
                            routine.startAtInstant().toEpochMilliseconds()
                        }
                        .take(2)
                }

                _viewState.value = LandingState.Success(
                    routines = displayRoutines,
                )
            }
        }
    }
}

sealed interface LandingState {
    object Loading : LandingState
    data class Success(
        val routines: List<Routine>
    ) : LandingState

    data class Error(val error: String) : LandingState
}
