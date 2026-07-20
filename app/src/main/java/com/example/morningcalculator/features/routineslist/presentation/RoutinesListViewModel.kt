package com.example.morningcalculator.features.routineslist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.shared.extensions.endAtInstant
import com.example.morningcalculator.shared.extensions.isCompleted
import com.example.morningcalculator.shared.extensions.isOngoing
import com.example.morningcalculator.shared.extensions.startAtInstant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoutinesListViewModel(
    val routineRepository: RoutineRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<RoutinesListState>(RoutinesListState.Loading)
    val viewState: StateFlow<RoutinesListState> = _viewState

    init {
        loadRoutines()
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            routineRepository.routinesFlow.collect {
                val sort = RoutinesListState.Sort.DEFAULT
                _viewState.value = RoutinesListState.Success(
                    routines = it, sorted = sortRoutines(sort, it), sort = sort
                )
            }
        }
    }
}

fun sortRoutines(
    sort: RoutinesListState.Sort, routines: List<Routine>
): List<Routine> {
    return when (sort.sortType) {
        RoutinesListState.Sort.SortType.DATE -> routines.sortedWith(compareBy({ routine ->
            when {
                routine.isOngoing() -> 0
                routine.isCompleted() -> 2
                else -> 1
            }
        }, { routine ->
            when {
                routine.isOngoing() -> routine.endAtInstant().toEpochMilliseconds()
                routine.isCompleted() -> -routine.endAtInstant().toEpochMilliseconds()
                else -> routine.startAtInstant().toEpochMilliseconds()
            }
        }))
    }
}

sealed interface RoutinesListState {
    object Loading : RoutinesListState

    data class Success(
        val routines: List<Routine>,
        val sorted: List<Routine>,
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
            ASCENDING, DESCENDING,
        }
    }
}