package com.xenikii.timecalculator.data.schedule.watchdog

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RoutineScheduleWatchdogScheduler {
    private const val UNIQUE_WORK_NAME = "routine_schedule_watchdog"

    fun ensureScheduled(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<RoutineScheduleReconcileWorker>(
            REPEAT_INTERVAL_HOURS,
            TimeUnit.HOURS,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }

    private const val REPEAT_INTERVAL_HOURS = 6L
}
