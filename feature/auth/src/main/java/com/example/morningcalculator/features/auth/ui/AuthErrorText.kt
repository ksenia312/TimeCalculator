package com.example.morningcalculator.features.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.morningcalculator.R
import com.example.morningcalculator.domain.model.AuthError
import com.example.morningcalculator.features.auth.presentation.AuthFormError

@Composable
internal fun authFormErrorText(error: AuthFormError): String = when (error) {
    AuthFormError.EmptyFields -> stringResource(R.string.auth_error_empty_fields)
    AuthFormError.PasswordsMismatch -> stringResource(R.string.auth_error_passwords_mismatch)
    is AuthFormError.Remote -> when (error.error) {
        AuthError.InvalidCredentials -> stringResource(R.string.auth_error_invalid_credentials)
        AuthError.UserAlreadyExists -> stringResource(R.string.auth_error_user_exists)
        AuthError.WeakPassword -> stringResource(R.string.auth_error_weak_password)
        AuthError.Network -> stringResource(R.string.auth_error_network)
        AuthError.RateLimited -> stringResource(R.string.auth_error_rate_limited)
        is AuthError.Unknown -> stringResource(R.string.auth_error_unknown)
    }
}
