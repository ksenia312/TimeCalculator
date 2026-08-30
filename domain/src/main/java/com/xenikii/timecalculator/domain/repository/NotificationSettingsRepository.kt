package com.xenikii.timecalculator.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    fun observeEnabled(): Flow<Boolean>
    fun isEnabled(): Boolean
    suspend fun setEnabled(enabled: Boolean)
    fun areSystemNotificationsAllowed(): Boolean
}

interface NotificationSettingsLocalDataSource {
    fun observeEnabled(): Flow<Boolean>
    fun isEnabled(): Boolean
    suspend fun setEnabled(enabled: Boolean)
}

interface NotificationPermissionChecker {
    fun areNotificationsAllowed(): Boolean
}
