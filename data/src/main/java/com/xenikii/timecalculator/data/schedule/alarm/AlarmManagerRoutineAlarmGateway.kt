package com.xenikii.timecalculator.data.schedule.alarm

import android.app.AlarmManager
import android.content.Context
import androidx.core.app.AlarmManagerCompat
import com.xenikii.timecalculator.domain.model.RoutineAlarmKind
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.repository.RoutineAlarmGateway
import kotlin.time.Instant

class AlarmManagerRoutineAlarmGateway(
    private val context: Context,
) : RoutineAlarmGateway {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    override fun canScheduleExactAlarms(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            true
        } else {
            alarmManager.canScheduleExactAlarms()
        }
    }

    override fun cancelRoutine(routineId: String, taskCount: Int) {
        cancelPendingIntent(routineId, RoutineAlarmKind.START, 0)
        for (index in 1 until taskCount) {
            cancelPendingIntent(routineId, RoutineAlarmKind.TASK, index)
        }
        cancelPendingIntent(routineId, RoutineAlarmKind.END, taskCount)
    }

    override fun schedule(plan: RoutineSchedule) {
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        if (plan.effectiveStart > now) {
            scheduleStart(plan)
        }
        plan.tasks.forEach { task ->
            if (task.index > 0 && task.start > now) {
                scheduleTask(plan.routineId, task.index, task.start.toEpochMilliseconds())
            }
        }
        if (plan.end > now) {
            scheduleEnd(plan.routineId, plan.tasks.size, plan.end.toEpochMilliseconds())
        }
    }

    private fun scheduleStart(plan: RoutineSchedule) {
        val triggerAtMillis = plan.effectiveStart.toEpochMilliseconds()
        val showIntent = buildRoutineDetailPendingIntent(context, plan.routineId)
        val operation = buildRoutineAlarmPendingIntent(
            context = context,
            routineId = plan.routineId,
            kind = RoutineAlarmKind.START,
            boundaryIndex = 0,
            triggerAtMillis = triggerAtMillis,
        )
        AlarmManagerCompat.setAlarmClock(
            alarmManager,
            triggerAtMillis,
            showIntent,
            operation,
        )
    }

    private fun scheduleTask(routineId: String, boundaryIndex: Int, triggerAtMillis: Long) {
        val operation = buildRoutineAlarmPendingIntent(
            context = context,
            routineId = routineId,
            kind = RoutineAlarmKind.TASK,
            boundaryIndex = boundaryIndex,
            triggerAtMillis = triggerAtMillis,
        )
        if (canScheduleExactAlarms()) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                operation,
            )
        } else {
            val showIntent = buildRoutineDetailPendingIntent(context, routineId)
            AlarmManagerCompat.setAlarmClock(
                alarmManager,
                triggerAtMillis,
                showIntent,
                operation,
            )
        }
    }

    private fun scheduleEnd(routineId: String, boundaryIndex: Int, triggerAtMillis: Long) {
        val operation = buildRoutineAlarmPendingIntent(
            context = context,
            routineId = routineId,
            kind = RoutineAlarmKind.END,
            boundaryIndex = boundaryIndex,
            triggerAtMillis = triggerAtMillis,
        )
        if (canScheduleExactAlarms()) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                operation,
            )
        } else {
            val showIntent = buildRoutineDetailPendingIntent(context, routineId)
            AlarmManagerCompat.setAlarmClock(
                alarmManager,
                triggerAtMillis,
                showIntent,
                operation,
            )
        }
    }

    private fun cancelPendingIntent(
        routineId: String,
        kind: RoutineAlarmKind,
        boundaryIndex: Int,
    ) {
        val pendingIntent = buildRoutineAlarmPendingIntent(
            context = context,
            routineId = routineId,
            kind = kind,
            boundaryIndex = boundaryIndex,
            triggerAtMillis = 0L,
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
