package com.xenikii.timecalculator.data.schedule.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.data.schedule.alarm.buildRoutineDetailPendingIntent
import com.xenikii.timecalculator.data.schedule.alarm.stableNotificationId
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.repository.NotificationSettingsLocalDataSource
import com.xenikii.timecalculator.domain.repository.RoutineNotificationGateway
import kotlin.time.Instant

class RoutineNotificationPresenter(
    private val context: Context,
    private val notificationSettings: NotificationSettingsLocalDataSource,
) : RoutineNotificationGateway {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannels()
    }

    override fun cancelRoutineNotifications(routineId: String) {
        notificationManager.cancel(progressNotificationId(routineId))
        notificationManager.cancel(alertNotificationId(routineId))
    }

    override fun cancelProgress(routineId: String) {
        notificationManager.cancel(progressNotificationId(routineId))
    }

    override fun postProgress(
        routine: Routine,
        plan: RoutineSchedule,
        now: Instant,
        alert: Boolean,
    ) {
        if (!notificationSettings.isEnabled()) return
        if (!notificationManager.areNotificationsEnabled()) return
        val task = plan.taskAt(now)
        if (task == null) {
            // No task covers `now` (routine ended or fell in a gap): never leave a stale
            // ongoing notification ticking down on an elapsed task.
            notificationManager.cancel(progressNotificationId(routine.id))
            return
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("${context.getString(R.string.notification_routine_title)}: ${routine.title}")
            .setContentText(task.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(task.title))
            .setContentIntent(buildRoutineDetailPendingIntent(context, routine.id))
            .setOngoing(true)
            .setOnlyAlertOnce(!alert)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(task.end.toEpochMilliseconds())
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()

        notificationManager.notify(progressNotificationId(routine.id), notification)
    }

    private fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_PROGRESS,
                context.getString(R.string.notification_channel_routine_progress),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_channel_routine_progress_description)
                setShowBadge(false)
            }
        )
    }

    private fun progressNotificationId(routineId: String): Int {
        return stableNotificationId(routineId, "progress")
    }

    private fun alertNotificationId(routineId: String): Int {
        return stableNotificationId(routineId, "alert")
    }

    companion object {
        private const val CHANNEL_PROGRESS = "routine_progress"
    }
}
