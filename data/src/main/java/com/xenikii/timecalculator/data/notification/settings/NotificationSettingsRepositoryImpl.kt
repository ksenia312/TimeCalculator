package com.xenikii.timecalculator.data.notification.settings

import com.xenikii.timecalculator.domain.repository.NotificationPermissionChecker
import com.xenikii.timecalculator.domain.repository.NotificationSettingsLocalDataSource
import com.xenikii.timecalculator.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow

class NotificationSettingsRepositoryImpl(
    private val localDataSource: NotificationSettingsLocalDataSource,
    private val permissionChecker: NotificationPermissionChecker,
) : NotificationSettingsRepository {

    override fun observeEnabled(): Flow<Boolean> = localDataSource.observeEnabled()

    override fun isEnabled(): Boolean = localDataSource.isEnabled()

    override suspend fun setEnabled(enabled: Boolean) {
        localDataSource.setEnabled(enabled)
    }

    override fun areSystemNotificationsAllowed(): Boolean =
        permissionChecker.areNotificationsAllowed()
}
