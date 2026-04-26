package com.example.morningcalculator.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.morningcalculator.app.notifications.RoutineNotificationPublisher
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.RoutineScheduleAnchor
import com.example.morningcalculator.data.local.RoutineAlarmReceiver
import com.example.morningcalculator.data.memory.RoutineAlarmMemoryDataSource
import com.example.morningcalculator.shared.extensions.withZeroSeconds
import kotlin.time.Duration
import kotlin.time.Instant

class RoutineAlarmSchedulerManager(
    private val context: Context,
    private val memoryDataSource: RoutineAlarmMemoryDataSource
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun rescheduleAll(routines: List<Routine>) {
        try {
            routines.forEach { scheduleRoutine(it) }
        } catch (t: Throwable) {
            Log.e(TAG, "rescheduleAll failed", t)
        }
    }

    fun scheduleRoutine(routine: Routine) {
        try {
            val normalized = routine.copy(scheduledAt = routine.scheduledAt.withZeroSeconds())

            cancelRoutine(normalized.id)

            if (normalized.data.isEmpty()) return

            val nowMillis = System.currentTimeMillis()

            val durations = normalized.data.map { it.effectiveDuration() }
            val total = durations.fold(Duration.ZERO) { acc, d -> acc + d }

            val startInstant = routineStartInstant(
                scheduledAt = normalized.scheduledAt,
                anchor = normalized.scheduledAtAnchor,
                totalDuration = total
            )

            val startMillis = startInstant.toEpochMilliseconds()
            val endMillis = startMillis + total.inWholeMilliseconds

            if (endMillis <= nowMillis) {
                RoutineNotificationPublisher.dismiss(context, normalized.id)
                return
            }

            val taskStartMillis = ArrayList<Long>(normalized.data.size)
            var cursor = startMillis
            durations.forEach { d ->
                taskStartMillis.add(cursor)
                cursor += d.inWholeMilliseconds
            }

            val isInProgress = nowMillis >= startMillis
            if (isInProgress) {
                val currentIndex = taskStartMillis.indexOfLast { it <= nowMillis }
                val safeCurrentIndex = currentIndex
                    .coerceAtLeast(0)
                    .coerceAtMost(normalized.data.lastIndex)

                RoutineNotificationPublisher.showTask(
                    context = context,
                    routine = normalized,
                    taskIndex = safeCurrentIndex,
                    shouldAlert = false
                )
            } else {
                RoutineNotificationPublisher.dismiss(context, normalized.id)
            }

            val stepCount = normalized.data.size + 1
            memoryDataSource.setStepCount(normalized.id, stepCount)

            taskStartMillis.forEachIndexed { index, triggerAt ->
                if (triggerAt > nowMillis) {
                    setAlarm(normalized.id, index, triggerAt)
                }
            }

            val endIndex = normalized.data.size
            setAlarm(normalized.id, endIndex, endMillis)
        } catch (t: Throwable) {
            Log.e(TAG, "scheduleRoutine failed", t)
        }
    }

    fun cancelRoutine(routineId: String) {
        try {
            val stepCount = memoryDataSource.getStepCount(routineId) ?: 64
            for (i in 0 until stepCount) {
                val pi = pendingIntent(routineId, i, PendingIntent.FLAG_NO_CREATE)
                if (pi != null) alarmManager.cancel(pi)
            }
            memoryDataSource.clear(routineId)
            RoutineNotificationPublisher.dismiss(context, routineId)
        } catch (t: Throwable) {
            Log.e(TAG, "cancelRoutine failed", t)
        }
    }

    private fun setAlarm(routineId: String, stepIndex: Int, triggerAtMillis: Long) {
        try {
            val pi = pendingIntent(routineId, stepIndex, PendingIntent.FLAG_UPDATE_CURRENT)
            if (pi == null) return

            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pi
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pi
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "setAlarm failed", t)
        }
    }

    private fun pendingIntent(
        routineId: String,
        stepIndex: Int,
        flag: Int
    ): PendingIntent? {
        return try {
            val intent = Intent(context, RoutineAlarmReceiver::class.java).apply {
                putExtra(RoutineAlarmReceiver.EXTRA_ROUTINE_ID, routineId)
                putExtra(RoutineAlarmReceiver.EXTRA_STEP_INDEX, stepIndex)
            }

            val requestCode = (routineId.hashCode() * 31) + stepIndex

            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flag or PendingIntent.FLAG_IMMUTABLE
            )
        } catch (t: Throwable) {
            Log.e(TAG, "pendingIntent failed", t)
            null
        }
    }

    private fun routineStartInstant(
        scheduledAt: Instant,
        anchor: RoutineScheduleAnchor,
        totalDuration: Duration
    ): Instant {
        return try {
            when (anchor) {
                RoutineScheduleAnchor.START -> scheduledAt
                RoutineScheduleAnchor.END -> {
                    Instant.fromEpochMilliseconds(
                        scheduledAt.toEpochMilliseconds() - totalDuration.inWholeMilliseconds
                    )
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "routineStartInstant failed", t)
            scheduledAt
        }
    }

    private fun RoutineLink.effectiveDuration(): Duration {
        return try {
            val direct = subData?.duration
            if (direct != null) return direct

            val fallback = task.dataSortedByDuration.firstOrNull()?.duration
            fallback ?: Duration.ZERO
        } catch (t: Throwable) {
            Log.e(TAG, "effectiveDuration failed", t)
            Duration.ZERO
        }
    }

    private companion object {
        private const val TAG = "RoutineAlarmScheduler"
    }
}