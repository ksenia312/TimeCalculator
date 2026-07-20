package com.example.morningcalculator.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.morningcalculator.R
import com.example.morningcalculator.app.MainActivity
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.RoutineScheduleAnchor
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

object RoutineNotificationPublisher {
    fun showTask(
        context: Context,
        routine: Routine,
        taskIndex: Int
    ) {
        RoutineNotificationChannels.ensureCreated(context)

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = routine.id.hashCode()
        val task = routine.data.getOrNull(taskIndex)?.task

        if (task == null) {
            manager.cancel(notificationId)
            return
        }

        val nowMillis = System.currentTimeMillis()
        val endsAtMillis = computeTaskEndMillis(routine, taskIndex)
        val endsAtText = formatTime(endsAtMillis)

        val remainingMinutes = ((endsAtMillis - nowMillis).coerceAtLeast(0L)) / 60000L
        val endsLine = if (remainingMinutes > 0) {
            context.getString(
                R.string.notification_ends_at_in,
                endsAtText,
                remainingMinutes.toInt()
            )
        } else {
            context.getString(R.string.notification_ends_at, endsAtText)
        }

        val nextTitle = routine.data
            .getOrNull(taskIndex + 1)
            ?.task
            ?.title
            ?: context.getString(R.string.notification_task_fallback)

        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val style = NotificationCompat.InboxStyle()
            .addLine(context.getString(R.string.notification_now, task.title))
            .addLine(context.getString(R.string.notification_next, nextTitle))
            .addLine(endsLine)

        manager.cancel(notificationId)

        val notification =
            NotificationCompat.Builder(context, RoutineNotificationChannels.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(routine.title)
                .setContentText(context.getString(R.string.notification_until, task.title, endsAtText))
                .setSubText(context.getString(R.string.notification_subtext_next, nextTitle))
                .setStyle(style)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .build()

        manager.notify(notificationId, notification)
    }

    fun dismiss(context: Context, routineId: String) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(routineId.hashCode())
    }

    private fun computeTaskEndMillis(routine: Routine, taskIndex: Int): Long {
        val durations = routine.data.map { it.effectiveDuration() }
        val total = durations.fold(Duration.ZERO) { acc, d -> acc + d }

        val scheduledAtMillis = (routine.scheduledAt.toEpochMilliseconds() / 60_000L) * 60_000L
        val startMillis = when (routine.scheduledAtAnchor) {
            RoutineScheduleAnchor.START -> scheduledAtMillis
            RoutineScheduleAnchor.END -> scheduledAtMillis - total.inWholeMilliseconds
        }

        val prefix = durations
            .take((taskIndex + 1).coerceAtMost(durations.size))
            .fold(Duration.ZERO) { acc, d -> acc + d }
            .inWholeMilliseconds

        return startMillis + prefix
    }

    private fun RoutineLink.effectiveDuration(): Duration {
        val direct = subData?.duration
        if (direct != null) return direct

        val fallback = task.dataSortedByDuration.firstOrNull()?.duration
        return fallback ?: Duration.ZERO
    }

    private fun formatTime(epochMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return java.time.Instant
            .ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}