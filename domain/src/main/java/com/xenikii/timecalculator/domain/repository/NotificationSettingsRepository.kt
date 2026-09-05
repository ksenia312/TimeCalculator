package com.xenikii.timecalculator.domain.repository

import com.xenikii.timecalculator.domain.model.NotificationMode
import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    fun observeEnabled(): Flow<Boolean>
    fun isEnabled(): Boolean
    suspend fun setEnabled(enabled: Boolean)
    fun observeMode(): Flow<NotificationMode>
    fun getMode(): NotificationMode
    suspend fun setMode(mode: NotificationMode)
    fun areSystemNotificationsAllowed(): Boolean
}

interface NotificationSettingsLocalDataSource {
    fun observeEnabled(): Flow<Boolean>
    fun isEnabled(): Boolean
    suspend fun setEnabled(enabled: Boolean)
    fun observeMode(): Flow<NotificationMode>
    fun getMode(): NotificationMode
    suspend fun setMode(mode: NotificationMode)
}

interface NotificationPermissionChecker {
    fun areNotificationsAllowed(): Boolean
}
