package com.xenikii.timecalculator.data.notification.settings

import android.content.Context
import androidx.core.content.edit
import com.xenikii.timecalculator.domain.repository.NotificationSettingsLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesNotificationSettingsLocalDataSource(
    context: Context,
) : NotificationSettingsLocalDataSource {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val enabledState = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, true))

    override fun observeEnabled(): Flow<Boolean> = enabledState.asStateFlow()

    override fun isEnabled(): Boolean = enabledState.value

    override suspend fun setEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ENABLED, enabled) }
        enabledState.value = enabled
    }

    private companion object {
        private const val PREFS_NAME = "notification_settings_prefs"
        private const val KEY_ENABLED = "notifications_enabled"
    }
}
