package com.xenikii.timecalculator.features.routineeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutinePauseState
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.usecase.ActivateRoutineUseCase
import com.xenikii.timecalculator.features.routineeditor.ui.RoutineEditorFormState
import com.xenikii.timecalculator.features.routineeditor.ui.applyRoutineEditorFormState
import com.xenikii.timecalculator.features.routineeditor.ui.toRoutineEditorFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditRoutineViewModel(
    private val routineId: String,
    private val routineRepository: RoutineRepository,
    private val activateRoutine: ActivateRoutineUseCase,
) : ViewModel() {

    private val _viewState = MutableStateFlow<EditRoutineViewState>(EditRoutineViewState.Loading)
    val viewState: StateFlow<EditRoutineViewState> = _viewState.asStateFlow()
    private var currentRoutine: Routine? = null

    private val _showLimitDialog = MutableStateFlow(false)
    val showLimitDialog: StateFlow<Boolean> = _showLimitDialog.asStateFlow()

    init {
        viewModelScope.launch {
            routineRepository.getRoutineFlow(routineId).collect { routine ->
                currentRoutine = routine
                _viewState.value = when {
                    routine == null -> EditRoutineViewState.Error
                    // Seed the editable form only on the first load. Later emissions keep the
                    // in-progress edits (e.g. selected days) instead of resetting them, but the
                    // pause state always tracks the latest value (it doesn't go through the form).
                    _viewState.value !is EditRoutineViewState.Success ->
                        EditRoutineViewState.Success(
                            form = routine.toRoutineEditorFormState(),
                            pauseState = routine.state,
                        )
                    else -> (_viewState.value as EditRoutineViewState.Success).copy(pauseState = routine.state)
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

    /** Pausing is unconditional (available to everyone); activating goes through
     * [ActivateRoutineUseCase] and may be blocked by the free-tier limit. */
    fun togglePause() {
        val routine = currentRoutine ?: return
        viewModelScope.launch {
            if (routine.isActive) {
                routineRepository.setPauseState(routineId, RoutinePauseState.PAUSED_MANUAL)
            } else if (activateRoutine(routineId) == ActivateRoutineUseCase.Result.BLOCKED_BY_LIMIT) {
                _showLimitDialog.value = true
            }
        }
    }

    fun dismissLimitDialog() {
        _showLimitDialog.value = false
    }
}

sealed interface EditRoutineViewState {
    data object Loading : EditRoutineViewState
    data class Success(val form: RoutineEditorFormState, val pauseState: RoutinePauseState) : EditRoutineViewState
    data object Error : EditRoutineViewState
}
