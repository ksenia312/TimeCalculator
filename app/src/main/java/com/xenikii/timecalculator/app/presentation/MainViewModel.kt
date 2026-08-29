package com.xenikii.timecalculator.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.AuthSessionState
import com.xenikii.timecalculator.domain.repository.AuthRepository
import com.xenikii.timecalculator.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class MainViewModel(
    authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainViewState())
    val uiState: StateFlow<MainViewState> = _uiState.asStateFlow()

    init {
        authRepository.observeAuthSessionState()
            .onEach(::handle)
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
}
