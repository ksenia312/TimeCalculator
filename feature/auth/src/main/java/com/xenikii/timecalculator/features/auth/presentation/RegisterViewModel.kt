package com.xenikii.timecalculator.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun onConfirmPasswordChange(value: String) =
        _state.update { it.copy(confirmPassword = value, error = null) }

    fun submit() {
        val current = _state.value
        if (current.isLoading) return
        if (current.email.isBlank() || current.password.isBlank() || current.confirmPassword.isBlank()) {
            _state.update { it.copy(error = AuthFormError.EmptyFields) }
            return
        }
        if (current.password != current.confirmPassword) {
            _state.update { it.copy(error = AuthFormError.PasswordsMismatch) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.signUp(current.email.trim(), current.password)
                .onSuccess { _state.update { it.copy(isLoading = false) } }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(isLoading = false, error = AuthFormError.Remote(throwable.toAuthError()))
                    }
                }
        }
    }
}
