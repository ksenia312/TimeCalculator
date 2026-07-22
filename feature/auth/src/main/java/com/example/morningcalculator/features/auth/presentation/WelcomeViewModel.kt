package com.example.morningcalculator.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.model.AuthSessionState
import com.example.morningcalculator.domain.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class WelcomeViewModel(
    authRepository: AuthRepository,
) : ViewModel() {

    val state: StateFlow<WelcomeState> =
        authRepository.observeAuthSessionState()
            .map { session ->
                when (session) {
                    is AuthSessionState.LoggedOut -> WelcomeState.Content
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
