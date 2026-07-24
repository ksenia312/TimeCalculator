package com.xenikii.timecalculator.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.AuthSessionState
import com.xenikii.timecalculator.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class MainViewModel(
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainViewState())
    val uiState: StateFlow<MainViewState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<MainEvent> = _events

    init {
        authRepository.observeAuthSessionState()
            .onEach(::handle)
            .launchIn(viewModelScope)
    }

    private fun handle(state: AuthSessionState) {
        val view = when (state) {
            AuthSessionState.Loading -> AuthViewState.Initializing
            AuthSessionState.LoggedIn,
            AuthSessionState.Recovering -> AuthViewState.LoggedIn

            is AuthSessionState.LoggedOut -> {
                if (state is AuthSessionState.LoggedOut.SessionExpired) {
                    _events.tryEmit(MainEvent.SessionExpired)
                }
                AuthViewState.LoggedOut
            }
        }
        _uiState.update { it.copy(authViewState = view, latestAuthSessionState = state) }
    }
}
