package com.xenikii.timecalculator.data.notification.settings

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.xenikii.timecalculator.domain.repository.NotificationPermissionChecker

class SystemNotificationPermissionChecker(
    context: Context,
) : NotificationPermissionChecker {

    private val notificationManager = NotificationManagerCompat.from(context)

    override fun areNotificationsAllowed(): Boolean =
        notificationManager.areNotificationsEnabled()
}
