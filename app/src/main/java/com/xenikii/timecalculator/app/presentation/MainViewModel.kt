package com.xenikii.timecalculator.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.data.schedule.ReconcileRoutinePauseForPremiumUseCase
import com.xenikii.timecalculator.domain.model.AuthSessionState
import com.xenikii.timecalculator.domain.model.PremiumEntitlementState
import com.xenikii.timecalculator.domain.repository.AuthRepository
import com.xenikii.timecalculator.domain.repository.FREE_ROUTINE_LIMIT
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
            val exists = ReconcileRoutinePauseForPremiumUseCase.hasUnresolvedLimitConflict(routines, entitlement)
            // Reset trigger, deliberately NOT nonManualCount: total routine count and entitlement
            // only change on real create/delete/premium events, never on a pause-state shuffle.
            val resolvedDurably = entitlement == PremiumEntitlementState.ACTIVE || routines.size <= FREE_ROUTINE_LIMIT
            android.util.Log.d("LIMIT_DEBUG", "MainViewModel: entitlement=$entitlement, totalRoutines=${routines.size}, acknowledged=$acknowledged, exists=$exists, resolvedDurably=$resolvedDurably @ ${System.currentTimeMillis()}")
            RoutineLimitConflict(exists = exists, resolvedDurably = resolvedDurably, acknowledged = acknowledged)
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
        if (conflict.resolvedDurably && conflict.acknowledged) {
            routineLimitResolutionRepository.setAcknowledged(false)
        }

        val needed = conflict.exists && !conflict.acknowledged
        android.util.Log.d("LIMIT_DEBUG", "MainViewModel: routineLimitResolutionNeeded=$needed @ ${System.currentTimeMillis()}")
        _uiState.update { it.copy(routineLimitResolutionNeeded = needed) }
    }

    private data class RoutineLimitConflict(
        val exists: Boolean,
        val resolvedDurably: Boolean,
        val acknowledged: Boolean,
    )
}
