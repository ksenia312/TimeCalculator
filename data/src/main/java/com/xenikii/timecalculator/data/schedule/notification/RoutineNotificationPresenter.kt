package com.xenikii.timecalculator.data.schedule.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.data.schedule.alarm.buildRoutineDetailPendingIntent
import com.xenikii.timecalculator.data.schedule.alarm.stableNotificationId
import com.xenikii.timecalculator.domain.model.NotificationMode
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
        notificationManager.cancel(routineStartedNotificationId(routineId))
        notificationManager.cancel(routineFinishedNotificationId(routineId))
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
        // START_AND_END mode is deliberately silent about individual tasks: no ongoing progress,
        // no per-task alert. postRoutineStarted()/postRoutineFinished() are its only notifications.
        if (notificationSettings.getMode() == NotificationMode.START_AND_END) return
        val task = plan.taskAt(now)
        if (task == null) {
            // No task covers `now` (routine ended or fell in a gap): never leave a stale
            // ongoing notification ticking down on an elapsed task.
            notificationManager.cancel(progressNotificationId(routine.id))
            return
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_progress_title, routine.title))
            .setContentText(task.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(task.title))
            .setContentIntent(buildRoutineDetailPendingIntent(context, routine.id))
            .setGroup(groupKey(routine.id))
            .setOngoing(true)
            // This notification must never itself alert: postAlert() below is now the single
            // source of sound/vibration/heads-up for a transition. Without this, both it and the
            // alert notification would alert together every time a task changes. setSilent(true)
            // forces silence regardless of channel importance, so this holds even on installs
            // that already created CHANNEL_PROGRESS at IMPORTANCE_HIGH before this change.
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(task.end.toEpochMilliseconds())
            .setAutoCancel(false)
            // CATEGORY_PROGRESS (download/install-style progress) is excluded by design from
            // being bridged to paired wearables (Wear OS / Samsung's Galaxy Wearable), the same
            // way a file-download notification never reaches a watch. This notification is a
            // countdown reminder of the current task, so CATEGORY_REMINDER both fits semantically
            // and is one of the categories that does get mirrored to a watch.
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(progressNotificationId(routine.id), notification)

        if (alert) {
            postAlert(routine, task.title)
        }
    }

    override fun postRoutineStarted(routine: Routine) {
        // In EVERY_TASK mode, postProgress() above already alerts for the first task starting -
        // this would just be a redundant second notification for the same moment.
        if (notificationSettings.getMode() == NotificationMode.EVERY_TASK) return
        postEvent(
            id = routineStartedNotificationId(routine.id),
            routine = routine,
            title = context.getString(R.string.notification_routine_started_title, routine.title),
            text = context.getString(R.string.notification_routine_started_text),
        )
    }

    override fun postRoutineFinished(routine: Routine) {
        postEvent(
            id = routineFinishedNotificationId(routine.id),
            routine = routine,
            title = context.getString(R.string.notification_routine_finished_title, routine.title),
            text = context.getString(R.string.notification_routine_finished_text),
        )
    }

    /**
     * Routine start/finish are the two moments the user most needs to notice, so they get their
     * own HIGH-importance channel with vibration on top of sound - louder than a plain task-change
     * alert. The sound still comes from NotificationChannel's default notification sound, which (
     * unlike RingtoneManager.TYPE_ALARM) plays once and stops on its own; nothing here loops it or
     * requires the user to silence it.
     */
    private fun postEvent(id: Int, routine: Routine, title: String, text: String) {
        if (!notificationSettings.isEnabled()) return
        if (!notificationManager.areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ROUTINE_EVENTS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(buildRoutineDetailPendingIntent(context, routine.id))
            .setGroup(groupKey(routine.id))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(id, notification)
    }

    /**
     * The ongoing progress notification above carries FLAG_ONGOING_EVENT, which companion-device
     * bridges (Wear OS, Samsung's Galaxy Wearable) treat as a local device-status indicator and
     * never forward to a paired watch, no matter its category - the same reason a download's
     * progress notification never reaches a watch. This is not a per-app setting anyone can turn
     * back on; it's the platform's default treatment of any ongoing notification. Only a normal,
     * dismissible, one-shot notification gets mirrored, so every real transition (routine start,
     * task change) also posts one of those here, on the previously-unused "alert" channel/id.
     */
    private fun postAlert(routine: Routine, taskTitle: String) {
        val alertNotification = NotificationCompat.Builder(context, CHANNEL_ALERT)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_alert_title, routine.title))
            .setContentText(taskTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(taskTitle))
            .setContentIntent(buildRoutineDetailPendingIntent(context, routine.id))
            .setGroup(groupKey(routine.id))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(alertNotificationId(routine.id), alertNotification)
    }

    private fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            // LOW: this channel is a silent, persistent status display now that CHANNEL_ALERT
            // owns alerting for real transitions - it should never itself make sound/vibrate.
            // (An already-existing HIGH channel from before this change can't be downgraded here;
            // postProgress()'s setSilent(true) is what covers those installs.)
            NotificationChannel(
                CHANNEL_PROGRESS,
                context.getString(R.string.notification_channel_routine_progress),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.notification_channel_routine_progress_description)
                setShowBadge(false)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERT,
                context.getString(R.string.notification_channel_task_alerts),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_channel_task_alerts_description)
            }
        )
        manager.createNotificationChannel(
            // HIGH + vibration: routine start/finish are the two moments that matter most, so
            // they alert more noticeably than a plain task-change (CHANNEL_ALERT, sound only).
            NotificationChannel(
                CHANNEL_ROUTINE_EVENTS,
                context.getString(R.string.notification_channel_routine_events),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_channel_routine_events_description)
                enableVibration(true)
            }
        )
    }

    private fun progressNotificationId(routineId: String): Int {
        return stableNotificationId(routineId, "progress")
    }

    private fun alertNotificationId(routineId: String): Int {
        return stableNotificationId(routineId, "alert")
    }

    private fun routineStartedNotificationId(routineId: String): Int {
        return stableNotificationId(routineId, "started")
    }

    private fun routineFinishedNotificationId(routineId: String): Int {
        return stableNotificationId(routineId, "finished")
    }

    private fun groupKey(routineId: String): String {
        return "routine_group_$routineId"
    }

    companion object {
        private const val CHANNEL_PROGRESS = "routine_progress"
        private const val CHANNEL_ALERT = "routine_task_alerts"
        private const val CHANNEL_ROUTINE_EVENTS = "routine_start_finish_events"
    }
}
