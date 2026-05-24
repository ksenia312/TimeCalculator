package com.example.morningcalculator.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object RoutineNotificationChannels {
    const val CHANNEL_ID = "routine_status"

    fun ensureCreated(context: Context) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val statusChannel = NotificationChannel(
            CHANNEL_ID,
            "Routines (Status)",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
            enableVibration(false)
            vibrationPattern = null
        }

        manager.createNotificationChannel(statusChannel)
    }
}