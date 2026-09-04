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
            REPEAT_INTERVAL_MINUTES,
            TimeUnit.MINUTES,
        ).build()

        // UPDATE (not KEEP) so that lowering the interval actually takes effect for installs that
        // already have the old, less frequent version of this work enqueued.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest,
        )
    }

    // This worker is the only safety net that can recover a progress notification stuck on an
    // elapsed task after a missed alarm, so it needs to run often - 15 minutes is WorkManager's
    // minimum periodic interval.
    private const val REPEAT_INTERVAL_MINUTES = 15L
}
