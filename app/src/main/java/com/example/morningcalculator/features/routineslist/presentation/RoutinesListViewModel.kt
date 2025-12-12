package com.example.morningcalculator.features.routineslist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.core.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoutinesListViewModel(
    val repository: TasksRepository,
    val routineRepository: RoutineRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<RoutinesListState>(RoutinesListState.Loading)
    val viewState: StateFlow<RoutinesListState> = _viewState

    init {
        loadRoutines()
    }

    fun addRoutine(request: RoutineRequest) {
        viewModelScope.launch {
            routineRepository.addRoutine(request)
            loadRoutines()
        }
    }

    fun editRoutine(request: Routine.Links) {
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

    private fun sortRoutines(
        sort: RoutinesListState.Sort,
        routines: List<Routine.Links>
    ): List<Routine.Links> {
        fun selector(routine: Routine): Long {
            return when (sort.sortType) {
                RoutinesListState.Sort.SortType.DATE -> routine.modifiedAt
            }
        }
        return when (sort.sortOrder) {
            RoutinesListState.Sort.SortOrder.ASCENDING -> routines.sortedBy { selector(it) }
            RoutinesListState.Sort.SortOrder.DESCENDING -> routines.sortedByDescending { selector(it) }
        }
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            routineRepository.routinesFlow.collect {
                val sort = RoutinesListState.Sort.DEFAULT
                _viewState.value = RoutinesListState.Success(
                    routines = it,
                    sorted = sortRoutines(sort, it),
                    sort = sort
                )
            }
        }
    }
}


sealed interface RoutinesListState {
    object Loading : RoutinesListState
    data class Success(
        val routines: List<Routine.Links>,
        val sorted: List<Routine.Links>,
        val sort: Sort = Sort.DEFAULT,
    ) : RoutinesListState

    data class Error(val error: String) : RoutinesListState

    data class Sort(
        val sortType: SortType,
        val sortOrder: SortOrder,
    ) {
        companion object {
            val DEFAULT = Sort(SortType.DATE, SortOrder.DESCENDING)
        }

        enum class SortType {
            DATE,
        }

        enum class SortOrder {
            ASCENDING,
            DESCENDING,
        }
    }
}