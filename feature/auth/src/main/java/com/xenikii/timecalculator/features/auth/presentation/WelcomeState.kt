package com.xenikii.timecalculator.features.auth.presentation

sealed interface WelcomeState {
    /** Waiting for the first auth state (or already logged in and awaiting redirect). */
    data object Loading : WelcomeState

    /** User is confirmed logged out — show the welcome actions. */
    data object Content : WelcomeState
}
