package com.xenikii.timecalculator.app.presentation

import com.xenikii.timecalculator.domain.model.AuthSessionState

data class MainViewState(
    val authViewState: AuthViewState = AuthViewState.Initializing,
    val latestAuthSessionState: AuthSessionState = AuthSessionState.Loading,
)

sealed interface AuthViewState {
    data object Initializing : AuthViewState
    data object LoggedIn : AuthViewState
    data object LoggedOut : AuthViewState
}

sealed interface MainEvent {
    data object SessionExpired : MainEvent
}
