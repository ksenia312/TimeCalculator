package com.example.morningcalculator.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.morningcalculator.R

object RoutineNotificationChannels {
    const val CHANNEL_ID = "routine_status"

    fun ensureCreated(context: Context) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val statusChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_routine_status),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
            enableVibration(false)
            vibrationPattern = null
        }

        manager.createNotificationChannel(statusChannel)
    }
}