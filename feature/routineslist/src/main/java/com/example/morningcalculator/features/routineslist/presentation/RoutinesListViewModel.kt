package com.example.morningcalculator.features.routineslist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineSchedule
import com.example.morningcalculator.domain.model.RoutineSchedulePhase
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.RoutineScheduleRepository
import com.example.morningcalculator.shared.viewitem.RoutineCardViewItem
import com.example.morningcalculator.shared.viewitem.toViewItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

class RoutinesListViewModel(
    val routineRepository: RoutineRepository,
    private val routineScheduleRepository: RoutineScheduleRepository,
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
                    items = sortRoutines(
                        sort,
                        it,
                        Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                        routineScheduleRepository
                    ),
                    sort = sort
                )
            }
        }
    }
}

fun sortRoutines(
    sort: RoutinesListState.Sort,
    routines: List<Routine>,
    now: Instant,
    routineScheduleRepository: RoutineScheduleRepository,
): List<RoutineListItemState> {
    val items = routines.map { routine ->
        val schedule = routineScheduleRepository.computeSchedule(routine, now)
        RoutineListItemState(
            routine = routine,
            schedule = schedule,
            cardViewItem = routine.toViewItem(schedule, now),
        )
    }

    return when (sort.sortType) {
        RoutinesListState.Sort.SortType.DATE -> items.sortedWith(compareBy({ item ->
            when (item.schedule.phaseAt(now)) {
                RoutineSchedulePhase.ACTIVE -> 0
                RoutineSchedulePhase.FUTURE -> 1
                RoutineSchedulePhase.FINISHED -> 2
            }
        }, { item ->
            when (item.schedule.phaseAt(now)) {
                RoutineSchedulePhase.ACTIVE -> item.schedule.end.toEpochMilliseconds()
                RoutineSchedulePhase.FINISHED -> -item.schedule.end.toEpochMilliseconds()
                RoutineSchedulePhase.FUTURE -> item.schedule.effectiveStart.toEpochMilliseconds()
            }
        }))
    }
}

data class RoutineListItemState(
    val routine: Routine,
    val schedule: RoutineSchedule,
    val cardViewItem: RoutineCardViewItem,
)

sealed interface RoutinesListState {
    object Loading : RoutinesListState

    data class Success(
        val items: List<RoutineListItemState>,
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