package com.xenikii.timecalculator.domain.model

sealed interface AuthSessionState {
    /** Session is still being restored from storage (cold start). */
    data object Loading : AuthSessionState

    /** Active, valid session. */
    data object LoggedIn : AuthSessionState

    /**
     * Token refresh temporarily failed, but the user is still considered logged in
     * (offline-friendly).
     */
    data object Recovering : AuthSessionState

    sealed interface LoggedOut : AuthSessionState {
        /** Explicit user sign-out. */
        data object UserInitiated : LoggedOut

        /** Session is definitively invalid (expired / refresh failed for good). */
        data object SessionExpired : LoggedOut
    }
}
