package com.xenikii.timecalculator.data.notification.settings

import android.content.Context
import androidx.core.content.edit
import com.xenikii.timecalculator.domain.model.NotificationMode
import com.xenikii.timecalculator.domain.repository.NotificationSettingsLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesNotificationSettingsLocalDataSource(
    context: Context,
) : NotificationSettingsLocalDataSource {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val enabledState = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, true))

    // Installs that predate notification modes never wrote KEY_MODE, and always behaved like
    // EVERY_TASK (progress notification + an alert per task) - so that's the default here,
    // both for those upgrading installs and for anyone who hasn't touched the setting yet.
    private val modeState = MutableStateFlow(readMode())

    override fun observeEnabled(): Flow<Boolean> = enabledState.asStateFlow()

    override fun isEnabled(): Boolean = enabledState.value

    override suspend fun setEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ENABLED, enabled) }
        enabledState.value = enabled
    }

    override fun observeMode(): Flow<NotificationMode> = modeState.asStateFlow()

    override fun getMode(): NotificationMode = modeState.value

    override suspend fun setMode(mode: NotificationMode) {
        prefs.edit { putString(KEY_MODE, mode.name) }
        modeState.value = mode
    }

    private fun readMode(): NotificationMode {
        val stored = prefs.getString(KEY_MODE, null) ?: return NotificationMode.EVERY_TASK
        return runCatching { NotificationMode.valueOf(stored) }.getOrDefault(NotificationMode.EVERY_TASK)
    }

    private companion object {
        private const val PREFS_NAME = "notification_settings_prefs"
        private const val KEY_ENABLED = "notifications_enabled"
        private const val KEY_MODE = "notifications_mode"
    }
}
