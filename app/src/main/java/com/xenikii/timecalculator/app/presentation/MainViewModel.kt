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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
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
            val nonManualCount = routines.count { it.state != com.xenikii.timecalculator.domain.model.RoutinePauseState.PAUSED_MANUAL }
            val exists = ReconcileRoutinePauseForPremiumUseCase.hasUnresolvedLimitConflict(routines, entitlement)
            android.util.Log.d("LIMIT_DEBUG", "MainViewModel: entitlement=$entitlement, nonManualCount=$nonManualCount, acknowledged=$acknowledged, exists=$exists @ ${System.currentTimeMillis()}")
            RoutineLimitConflict(exists = exists, acknowledged = acknowledged)
        }
            .debounce(CONFLICT_DEBOUNCE_MILLIS.milliseconds)
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
        // Resets the acknowledgment so a later recurrence prompts fresh again.
        if (!conflict.exists && conflict.acknowledged) {
            routineLimitResolutionRepository.setAcknowledged(false)
        }

        val needed = conflict.exists && !conflict.acknowledged
        android.util.Log.d("LIMIT_DEBUG", "MainViewModel: routineLimitResolutionNeeded=$needed @ ${System.currentTimeMillis()}")
        _uiState.update { it.copy(routineLimitResolutionNeeded = needed) }
    }

    private data class RoutineLimitConflict(
        val exists: Boolean,
        val acknowledged: Boolean,
    )

    private companion object {
        const val CONFLICT_DEBOUNCE_MILLIS = 500L
    }
}
