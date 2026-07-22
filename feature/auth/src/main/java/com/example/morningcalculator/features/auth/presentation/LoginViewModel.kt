package com.example.morningcalculator.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.domain.model.AuthError
import com.example.morningcalculator.domain.model.AuthException
import com.example.morningcalculator.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun submit() {
        val current = _state.value
        if (current.isLoading) return
        if (current.email.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(error = AuthFormError.EmptyFields) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.signIn(current.email.trim(), current.password)
                .onSuccess { _state.update { it.copy(isLoading = false) } }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(isLoading = false, error = AuthFormError.Remote(throwable.toAuthError()))
                    }
                }
        }
    }
}

internal fun Throwable.toAuthError(): AuthError =
    (this as? AuthException)?.error ?: AuthError.Unknown(this)
