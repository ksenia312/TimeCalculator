package com.example.morningcalculator.features.home.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.core.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    val repository: TasksRepository,
    val routineRepository: RoutineRepository,
) : ViewModel(){

    private val _viewState = MutableStateFlow<HomeViewState>(HomeViewState.Loading)
    val viewState: StateFlow<HomeViewState> = _viewState

    init {
        loadRoutines()
    }

    fun addRoutine(request: RoutineRequest) {
        viewModelScope.launch {
            routineRepository.addRoutine(request)
            loadRoutines()
        }
    }

    fun editRoutine(request: Routine) {
        viewModelScope.launch {
            routineRepository.updateRoutine(request)
            loadRoutines()
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
            loadRoutines()
        }
    }

    fun clearTasks() {
        viewModelScope.launch {
            repository.clearTasks()
            loadRoutines()
        }
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            routineRepository.routinesFlow().collect {
                _viewState.value = HomeViewState.Success(it)
            }
        }
    }
}


sealed interface HomeViewState {
    object Loading : HomeViewState
    data class Success(val tasks: List<Routine>) : HomeViewState
    data class Error(val error: String) : HomeViewState
}