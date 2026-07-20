package com.example.morningcalculator.features.routineeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.model.RoutineRequest
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.features.routineeditor.ui.RoutineEditorFormState
import com.example.morningcalculator.features.routineeditor.ui.toScheduledAtInstant
import com.example.morningcalculator.shared.extensions.toHexString
import com.example.morningcalculator.shared.utils.RoutineColorPicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateRoutineViewModel(
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow(RoutineEditorFormState())
    val viewState: StateFlow<RoutineEditorFormState> = _viewState.asStateFlow()

    fun onStateChange(newState: RoutineEditorFormState) {
        _viewState.value = newState
    }

    fun saveRoutine() {
        val state = _viewState.value
        viewModelScope.launch {
            routineRepository.addRoutine(
                RoutineRequest(
                    title = state.title,
                    scheduledAt = state.toScheduledAtInstant(),
                    scheduledAtAnchor = state.anchor,
                    color = RoutineColorPicker.pick().toHexString(),
                )
            )
        }
        _viewState.update { it.copy(isVisible = false) }
    }
}
