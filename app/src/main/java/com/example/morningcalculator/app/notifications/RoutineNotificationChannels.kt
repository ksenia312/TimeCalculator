package com.example.morningcalculator.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager

object RoutineNotificationChannels {
    const val ALARM_CHANNEL_ID = "routine_alarm"
    const val STATUS_CHANNEL_ID = "routine_status"

    fun ensureCreated(context: Context) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Routines (Alarm)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(soundUri, attrs)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            setBypassDnd(false)
        }

        val statusChannel = NotificationChannel(
            STATUS_CHANNEL_ID,
            "Routines (Status)",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
            enableVibration(false)
            vibrationPattern = null
        }

        manager.createNotificationChannel(alarmChannel)
        manager.createNotificationChannel(statusChannel)
    }
}