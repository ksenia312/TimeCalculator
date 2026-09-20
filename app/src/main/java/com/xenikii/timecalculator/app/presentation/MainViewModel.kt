package com.xenikii.timecalculator.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.data.schedule.ReconcileRoutinePauseForPremiumUseCase
import com.xenikii.timecalculator.domain.model.AuthSessionState
import com.xenikii.timecalculator.domain.model.PremiumEntitlementState
import com.xenikii.timecalculator.domain.repository.AuthRepository
import com.xenikii.timecalculator.domain.repository.OnboardingRepository
import com.xenikii.timecalculator.domain.repository.PremiumRepository
import com.xenikii.timecalculator.domain.repository.RoutineLimitResolutionRepository
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class MainViewModel(
    authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository,
    routineRepository: RoutineRepository,
    premiumRepository: PremiumRepository,
    private val routineLimitResolutionRepository: RoutineLimitResolutionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainViewState())
    val uiState: StateFlow<MainViewState> = _uiState.asStateFlow()

    init {
        authRepository.observeAuthSessionState()
            .onEach(::handle)
            .launchIn(viewModelScope)

        combine(
            routineRepository.routinesFlow,
            premiumRepository.observeEntitlementState(),
            routineLimitResolutionRepository.observeAcknowledged(),
        ) { routines, entitlement, acknowledged ->
            RoutineLimitConflict(
                exists = ReconcileRoutinePauseForPremiumUseCase.hasUnresolvedLimitConflict(routines, entitlement),
                acknowledged = acknowledged,
            )
        }
            .distinctUntilChanged()
            .onEach(::handleRoutineLimitConflict)
            .launchIn(viewModelScope)
    }

    fun isOnboardingCompleted(): Boolean = onboardingRepository.isCompleted()

    private fun handle(state: AuthSessionState) {
        val view = when (state) {
            AuthSessionState.Loading -> AuthViewState.Initializing
            AuthSessionState.LoggedIn,
            AuthSessionState.Recovering -> AuthViewState.LoggedIn

            AuthSessionState.LoggedOut.UserInitiated -> AuthViewState.LoggedOut.UserInitiated
            AuthSessionState.LoggedOut.SessionExpired -> AuthViewState.LoggedOut.SessionExpired
        }
        _uiState.update { it.copy(authViewState = view, latestAuthSessionState = state) }
    }

    private suspend fun handleRoutineLimitConflict(conflict: RoutineLimitConflict) {
        if (!conflict.exists && conflict.acknowledged) {
            // The conflict resolved on its own (premium returned, or routines were deleted/
            // manually paused down to the limit) - clear the acknowledgment so a future
            // recurrence of the same conflict (premium expiring again later) prompts fresh again
            // instead of staying silently acknowledged forever.
            routineLimitResolutionRepository.setAcknowledged(false)
        }

        _uiState.update { it.copy(routineLimitResolutionNeeded = conflict.exists && !conflict.acknowledged) }
    }

    private data class RoutineLimitConflict(
        val exists: Boolean,
        val acknowledged: Boolean,
    )
}
