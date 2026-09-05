package com.xenikii.timecalculator.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.NotificationMode
import com.xenikii.timecalculator.domain.model.User
import com.xenikii.timecalculator.domain.repository.AuthRepository
import com.xenikii.timecalculator.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val logoutUseCase: suspend () -> Result<Unit>,
    private val authRepository: AuthRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val refreshNotifications: suspend () -> Unit,
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        SettingsViewState(
            notificationsEnabled = notificationSettingsRepository.isEnabled(),
            notificationMode = notificationSettingsRepository.getMode(),
            areSystemNotificationsAllowed = notificationSettingsRepository.areSystemNotificationsAllowed(),
        )
    )
    val viewState: StateFlow<SettingsViewState> = _viewState.asStateFlow()

    init {
        startObservingUser()
        startObservingNotificationSettings()
        startObservingNotificationMode()
    }

    fun logout() {
        if (_viewState.value.isLoggingOut) return
        viewModelScope.launch {
            _viewState.update { it.copy(isLoggingOut = true) }
            val result = logoutUseCase()
            if (result.isFailure) {
                _viewState.update { it.copy(isLoggingOut = false) }
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationSettingsRepository.setEnabled(enabled)
        }
    }

    fun setNotificationMode(mode: NotificationMode) {
        viewModelScope.launch {
            notificationSettingsRepository.setMode(mode)
        }
    }

    fun refreshNotificationPermission() {
        val previouslyAllowed = _viewState.value.areSystemNotificationsAllowed
        val allowed = notificationSettingsRepository.areSystemNotificationsAllowed()
        _viewState.update { it.copy(areSystemNotificationsAllowed = allowed) }

        if (allowed && !previouslyAllowed && notificationSettingsRepository.isEnabled()) {
            viewModelScope.launch {
                refreshNotifications()
            }
        }
    }

    private fun startObservingUser() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _viewState.update { it.copy(user = user) }
            }
        }
    }

    private fun startObservingNotificationSettings() {
        viewModelScope.launch {
            notificationSettingsRepository.observeEnabled().collect { enabled ->
                _viewState.update { it.copy(notificationsEnabled = enabled) }
            }
        }
    }

    private fun startObservingNotificationMode() {
        viewModelScope.launch {
            notificationSettingsRepository.observeMode().collect { mode ->
                _viewState.update { it.copy(notificationMode = mode) }
            }
        }
    }
}

data class SettingsViewState(
    val isLoggingOut: Boolean = false,
    val user: User? = null,
    val notificationsEnabled: Boolean = false,
    val notificationMode: NotificationMode = NotificationMode.EVERY_TASK,
    val areSystemNotificationsAllowed: Boolean = true,
) {
    val isNotificationsSwitchOn: Boolean
        get() = notificationsEnabled && areSystemNotificationsAllowed
}
