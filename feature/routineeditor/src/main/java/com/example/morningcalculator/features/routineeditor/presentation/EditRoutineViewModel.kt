package com.example.morningcalculator.features.routineeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.features.routineeditor.ui.RoutineEditorFormState
import com.example.morningcalculator.features.routineeditor.ui.applyRoutineEditorFormState
import com.example.morningcalculator.features.routineeditor.ui.toRoutineEditorFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditRoutineViewModel(
    private val routineId: String,
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<EditRoutineViewState>(EditRoutineViewState.Loading)
    val viewState: StateFlow<EditRoutineViewState> = _viewState.asStateFlow()
    private var currentRoutine: Routine? = null

    init {
        viewModelScope.launch {
            routineRepository.getRoutineFlow(routineId).collect { routine ->
                currentRoutine = routine
                _viewState.value = if (routine == null) {
                    EditRoutineViewState.Error
                } else {
                    EditRoutineViewState.Success(routine.toRoutineEditorFormState())
                }
            }
        }
    }

    fun onStateChange(newState: RoutineEditorFormState) {
        val current = _viewState.value as? EditRoutineViewState.Success ?: return
        _viewState.value = current.copy(form = newState)
    }

    fun saveRoutine() {
        val form = (_viewState.value as? EditRoutineViewState.Success)?.form ?: return
        val routine = currentRoutine ?: return
        viewModelScope.launch {
            routineRepository.updateRoutine(routine.applyRoutineEditorFormState(form))
        }
    }

    fun deleteRoutine() {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
        }
    }
}

sealed interface EditRoutineViewState {
    data object Loading : EditRoutineViewState
    data class Success(val form: RoutineEditorFormState) : EditRoutineViewState
    data object Error : EditRoutineViewState
}
