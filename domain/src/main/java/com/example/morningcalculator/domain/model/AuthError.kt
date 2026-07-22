package com.example.morningcalculator.domain.model

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object UserAlreadyExists : AuthError
    data object WeakPassword : AuthError
    data object Network : AuthError
    data object RateLimited : AuthError
    data class Unknown(val cause: Throwable?) : AuthError
}

class AuthException(val error: AuthError) : Exception()
