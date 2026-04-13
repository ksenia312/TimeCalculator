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
                val sorted = routines.sortedBy { it.startAtInstant().toEpochMilliseconds() }

                val ongoing = sorted.filter { it.isOngoing() }
                val upcoming = sorted.filter { !it.isOngoing() }

                val displayRoutines = (ongoing + upcoming)
                    .distinct()
                    .take(2)
                    .ifEmpty { sorted.take(2) }

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
