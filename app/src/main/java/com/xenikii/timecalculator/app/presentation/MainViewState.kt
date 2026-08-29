package com.xenikii.timecalculator.app.presentation

import com.xenikii.timecalculator.domain.model.AuthSessionState

data class MainViewState(
    val authViewState: AuthViewState = AuthViewState.Initializing,
    val latestAuthSessionState: AuthSessionState = AuthSessionState.Loading,
)

sealed interface AuthViewState {
    data object Initializing : AuthViewState
    data object LoggedIn : AuthViewState
    sealed interface LoggedOut : AuthViewState {
        data object UserInitiated : LoggedOut
        data object SessionExpired : LoggedOut
    }
}
