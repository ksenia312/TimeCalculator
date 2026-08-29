package com.xenikii.timecalculator.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.AuthSessionState
import com.xenikii.timecalculator.domain.repository.AuthRepository
import com.xenikii.timecalculator.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class WelcomeViewModel(
    authRepository: AuthRepository,
    onboardingRepository: OnboardingRepository,
) : ViewModel() {

    val state: StateFlow<WelcomeState> =
        authRepository.observeAuthSessionState()
            .map { session ->
                when (session) {
                    is AuthSessionState.LoggedOut -> {
                        if (onboardingRepository.isCompleted()) {
                            WelcomeState.Content
                        } else {
                            WelcomeState.Loading
                        }
                    }
                    AuthSessionState.Loading,
                    AuthSessionState.LoggedIn,
                    AuthSessionState.Recovering -> WelcomeState.Loading
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = WelcomeState.Loading,
            )
}
