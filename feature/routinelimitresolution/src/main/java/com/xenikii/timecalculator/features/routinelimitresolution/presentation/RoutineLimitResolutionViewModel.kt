package com.xenikii.timecalculator.features.routinelimitresolution.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutinePauseState
import com.xenikii.timecalculator.domain.repository.FREE_ROUTINE_LIMIT
import com.xenikii.timecalculator.domain.repository.PremiumRepository
import com.xenikii.timecalculator.domain.repository.RoutineLimitResolutionRepository
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lets a user whose premium expired (and who now has more than [FREE_ROUTINE_LIMIT]
 * non-manually-paused routines) reconsider which stay active. Shows every routine, including
 * ones the user manually paused themselves - they must be able to pull one of those back into
 * the active set. Reuses stage 1's [priorityComparator] for both the preselection and the row
 * ordering - no separate selection logic is computed here; saving just persists the user's
 * manual checkbox choice via [RoutineRepository.setPauseState], never touching a routine that was
 * already a deliberate [RoutinePauseState.PAUSED_MANUAL] and wasn't selected.
 *
 * Leaving the screen - both after a successful save and the moment premium comes back - is driven
 * centrally by AppNavigator reacting to [RoutineLimitResolutionRepository]/entitlement state (the
 * same signals that decided to show this screen in the first place). This ViewModel has no
 * navigation concept of its own.
 */
class RoutineLimitResolutionViewModel(
    private val routineRepository: RoutineRepository,
    private val premiumRepository: PremiumRepository,
    private val routineLimitResolutionRepository: RoutineLimitResolutionRepository,
    private val priorityComparator: Comparator<Routine>,
) : ViewModel() {

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    private val _saveState = MutableStateFlow(SaveState())

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _restoreResult = MutableStateFlow<RestoreResult?>(null)
    val restoreResult: StateFlow<RestoreResult?> = _restoreResult.asStateFlow()

    val viewState: StateFlow<RoutineLimitResolutionViewState> = combine(
        routineRepository.routinesFlow,
        _selectedIds,
        _saveState,
    ) { routines, selectedIds, saveState ->
        // All routines are shown, including manually-paused ones - the user must be able to pull
        // a routine they put on vacation themselves back into the active set.
        RoutineLimitResolutionViewState.Content(
            items = routines
                .sortedWith(priorityComparator)
                .map { routine ->
                    RoutineLimitResolutionItem(
                        id = routine.id,
                        title = routine.title,
                        lastTriggeredAt = routine.lastTriggeredAt,
                        isSelected = routine.id in selectedIds,
                    )
                },
            selectedCount = selectedIds.size,
            canSave = selectedIds.size in 1..FREE_ROUTINE_LIMIT && !saveState.inProgress,
            saveState = saveState,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RoutineLimitResolutionViewState.Loading,
        )

    init {
        // Preselect exactly once, from the first available snapshot - never re-run on later
        // routinesFlow emissions, or the user's own toggles would keep getting overwritten by
        // background changes (auto-pause reconciling, a routine finishing, etc).
        viewModelScope.launch {
            val routines = routineRepository.routinesFlow.first()
            val currentlyActiveIds = routines
                .filter { it.state == RoutinePauseState.ACTIVE }
                .map { it.id }
            _selectedIds.value = currentlyActiveIds.ifEmpty {
                // Degenerate fallback (nothing at all is active) - fall back to the same ranking
                // the premium-expiry safety net itself uses.
                routines
                    .sortedWith(priorityComparator)
                    .take(FREE_ROUTINE_LIMIT)
                    .map { it.id }
            }.toSet()
        }
    }

    fun toggleSelection(id: String) {
        _selectedIds.update { current ->
            if (id in current) current - id else current + id
        }
    }

    fun restorePurchases() {
        if (_isRestoring.value) return
        viewModelScope.launch {
            _isRestoring.value = true
            val restored = premiumRepository.restore()
            _isRestoring.value = false
            _restoreResult.value = if (restored) RestoreResult.RESTORED else RestoreResult.NOTHING_TO_RESTORE
        }
    }

    fun onRestoreResultShown() {
        _restoreResult.value = null
    }

    fun save() {
        val content = viewState.value as? RoutineLimitResolutionViewState.Content ?: return
        if (!content.canSave) return

        viewModelScope.launch {
            _saveState.value = SaveState(inProgress = true)
            val selected = _selectedIds.value

            // Not wrapped in a single DB transaction: a crash mid-loop can't leave the app in an
            // invalid state anyway, since stage 1's RoutineAutoPauseCoordinator reconciles active
            // count back down to the free limit on its own the next time it observes
            // routinesFlow - this loop only chooses *which* routines end up active among an
            // already-safe set.
            val succeeded = runCatching {
                routineRepository.routinesFlow.first().forEach { routine ->
                    val targetState = when {
                        routine.id in selected -> RoutinePauseState.ACTIVE
                        // Not selected because it was already a deliberate vacation, not because
                        // of the limit - leave it exactly as the user set it themselves.
                        routine.state == RoutinePauseState.PAUSED_MANUAL -> RoutinePauseState.PAUSED_MANUAL
                        // Not selected and not a manual pause - it's excluded by the limit.
                        else -> RoutinePauseState.PAUSED_AUTO
                    }
                    routineRepository.setPauseState(routine.id, targetState)
                }
                routineLimitResolutionRepository.setAcknowledged(true)
            }.isSuccess

            _saveState.value = SaveState(failed = !succeeded)
        }
    }
}

enum class RestoreResult {
    RESTORED,
    NOTHING_TO_RESTORE,
}
