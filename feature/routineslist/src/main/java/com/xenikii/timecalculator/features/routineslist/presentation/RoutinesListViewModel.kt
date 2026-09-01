package com.xenikii.timecalculator.features.routineslist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.model.RoutineSchedulePhase
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import com.xenikii.timecalculator.shared.viewitem.RoutineCardViewItem
import com.xenikii.timecalculator.shared.viewitem.toViewItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

class RoutinesListViewModel(
    val routineRepository: RoutineRepository,
    private val routineScheduleRepository: RoutineScheduleRepository,
) : ViewModel() {

    val viewState: StateFlow<RoutinesListState> = routineRepository.routinesFlow
        .map { routines ->
            runCatching {
                val sort = RoutinesListState.Sort.DEFAULT
                RoutinesListState.Success(
                    items = sortRoutines(
                        sort,
                        routines,
                        Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                        routineScheduleRepository,
                    ),
                    sort = sort,
                )
            }
        }
        .scan<Result<RoutinesListState.Success>, RoutinesListState>(RoutinesListState.Loading) { previous, result ->
            result.getOrElse { error ->
                previous as? RoutinesListState.Success
                    ?: RoutinesListState.Error(error.message.orEmpty())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RoutinesListState.Loading,
        )

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds

    fun toggleSelection(id: String) {
        _selectedIds.update { current ->
            if (id in current) current - id else current + id
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                routineRepository.deleteRoutine(id)
            }
            _selectedIds.value = emptySet()
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