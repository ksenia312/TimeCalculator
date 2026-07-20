package com.example.morningcalculator.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.RoutineRequest
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.core.repository.TasksRepository
import com.example.morningcalculator.features.home.ui.components.HomeTab
import com.example.morningcalculator.features.home.ui.components.RoutineDialogViewState
import com.example.morningcalculator.features.home.ui.components.toScheduledAtInstant
import com.example.morningcalculator.shared.extensions.toHexString
import com.example.morningcalculator.shared.utils.RoutineColorPicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    val routineRepository: RoutineRepository,
    val tasksRepository: TasksRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow(HomeViewState())
    val uiState: StateFlow<HomeViewState> = _viewState.asStateFlow()

    fun addRoutine(request: RoutineRequest) {
        viewModelScope.launch {
            routineRepository.addRoutine(request)
        }
        onTabSelected(HomeTab.ROUTINES)
    }

    fun addNewTask(request: TaskRequest) {
        viewModelScope.launch {
            tasksRepository.addTask(request)
        }
        onTabSelected(HomeTab.TASKS)
    }

    fun onTabSelected(tab: HomeTab) {
        _viewState.update { it.copy(selectedTab = tab) }
    }

    fun onAddRoutineClick() {
        _viewState.update {
            it.copy(
                routineDialogViewState = RoutineDialogViewState(
                    isVisible = true
                )
            )
        }
    }

    fun onRoutineDialogDismiss() {
        _viewState.update {
            it.copy(
                routineDialogViewState = it.routineDialogViewState.copy(
                    isVisible = false
                )
            )
        }
    }

    fun onRoutineDialogViewStateChange(routineDialogViewState: RoutineDialogViewState) {
        _viewState.update {
            it.copy(
                routineDialogViewState = routineDialogViewState
            )
        }
    }

    fun onRoutineDialogConfirm() {
        val state = uiState.value.routineDialogViewState
        val request = RoutineRequest(
            title = state.title,
            scheduledAt = state.toScheduledAtInstant(),
            scheduledAtAnchor = state.anchor,
            color = RoutineColorPicker.pick().toHexString()
        )

        viewModelScope.launch {
            routineRepository.addRoutine(request)
        }
        _viewState.update {
            it.copy(
                selectedTab = HomeTab.ROUTINES,
                routineDialogViewState = it.routineDialogViewState.copy(
                    isVisible = false
                )
            )
        }
    }
}

data class HomeViewState(
    val selectedTab: HomeTab = HomeTab.LANDING,
    val routineDialogViewState: RoutineDialogViewState = RoutineDialogViewState(),
)
