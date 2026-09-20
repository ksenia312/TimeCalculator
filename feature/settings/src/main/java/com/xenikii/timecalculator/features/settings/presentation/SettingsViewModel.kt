package com.xenikii.timecalculator.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.NotificationMode
import com.xenikii.timecalculator.domain.model.PremiumStatus
import com.xenikii.timecalculator.domain.model.effectiveNotificationMode
import com.xenikii.timecalculator.domain.model.User
import com.xenikii.timecalculator.domain.repository.AuthRepository
import com.xenikii.timecalculator.domain.repository.NotificationSettingsRepository
import com.xenikii.timecalculator.domain.repository.PremiumRepository
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
    private val premiumRepository: PremiumRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        SettingsViewState(
            notificationsEnabled = notificationSettingsRepository.isEnabled(),
            notificationMode = notificationSettingsRepository.getMode(),
            areSystemNotificationsAllowed = notificationSettingsRepository.areSystemNotificationsAllowed(),
        )
    )
    val viewState: StateFlow<SettingsViewState> = _viewState.asStateFlow()

    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    init {
        startObservingUser()
        startObservingNotificationSettings()
        startObservingNotificationMode()
        startObservingPremium()
    }

    fun logout() {
        if (_viewState.value.isLoggingOut) return
        android.util.Log.d("LOGOUT_DEBUG", "SettingsViewModel.logout(): start @ ${System.currentTimeMillis()}")
        viewModelScope.launch {
            _viewState.update { it.copy(isLoggingOut = true) }
            val result = logoutUseCase()
            android.util.Log.d("LOGOUT_DEBUG", "SettingsViewModel.logout(): logoutUseCase() returned $result @ ${System.currentTimeMillis()}")
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
        if (mode == NotificationMode.EVERY_TASK && !_viewState.value.premiumStatus.isActive) {
            _showPaywall.value = true
            return
        }
        viewModelScope.launch {
            notificationSettingsRepository.setMode(mode)
        }
    }

    fun onPaywallShown() {
        _showPaywall.value = false
    }

    fun restorePurchases() {
        if (_viewState.value.isRestoringPurchases) return
        viewModelScope.launch {
            _viewState.update { it.copy(isRestoringPurchases = true) }
            val restoredToPremium = premiumRepository.restore()
            _viewState.update {
                it.copy(
                    isRestoringPurchases = false,
                    restoreResult = if (restoredToPremium) RestoreResult.Restored else RestoreResult.NothingToRestore,
                )
            }
        }
    }

    fun onRestoreResultShown() {
        _viewState.update { it.copy(restoreResult = null) }
    }

    private fun startObservingPremium() {
        viewModelScope.launch {
            premiumRepository.observePremiumStatus().collect { premiumStatus ->
                _viewState.update { it.copy(premiumStatus = premiumStatus) }
            }
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
    val premiumStatus: PremiumStatus = PremiumStatus.None,
    val isRestoringPurchases: Boolean = false,
    val restoreResult: RestoreResult? = null,
) {
    val isNotificationsSwitchOn: Boolean
        get() = notificationsEnabled && areSystemNotificationsAllowed

    /** What the UI should show as selected: the saved [notificationMode] downgraded to
     * START_AND_END while not premium, mirroring what RoutineNotificationPresenter actually does
     * - so the screen never shows neither option selected. */
    val effectiveNotificationMode: NotificationMode
        get() = notificationMode.effectiveNotificationMode(premiumStatus.isActive)
}

enum class RestoreResult {
    Restored,
    NothingToRestore,
}
