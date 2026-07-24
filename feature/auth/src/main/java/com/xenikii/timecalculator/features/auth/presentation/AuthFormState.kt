package com.xenikii.timecalculator.features.auth.presentation

import com.xenikii.timecalculator.domain.model.AuthError

sealed interface AuthFormError {
    data object EmptyFields : AuthFormError
    data object PasswordsMismatch : AuthFormError
    data class Remote(val error: AuthError) : AuthFormError
}

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: AuthFormError? = null,
)
