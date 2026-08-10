package com.xenikii.timecalculator.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.User
import com.xenikii.timecalculator.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val logoutUseCase: suspend () -> Result<Unit>,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState: StateFlow<SettingsViewState> = _viewState.asStateFlow()

    init {
        startObservingUser()
    }

    fun logout() {
        if (_viewState.value.isLoggingOut) return
        viewModelScope.launch {
            _viewState.update { it.copy(isLoggingOut = true) }
            logoutUseCase()
            _viewState.update { it.copy(isLoggingOut = false) }
        }
    }

    private fun startObservingUser() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _viewState.update { it.copy(user = user) }
            }
        }
    }
}

data class SettingsViewState(
    val isLoggingOut: Boolean = false,
    val user: User? = null,
)
