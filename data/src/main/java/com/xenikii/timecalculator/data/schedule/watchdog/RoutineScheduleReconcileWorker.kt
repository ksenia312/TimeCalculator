package com.xenikii.timecalculator.data.schedule.watchdog

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Instant

class RoutineScheduleReconcileWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val routineRepository: RoutineRepository by inject()
    private val scheduleRepository: RoutineScheduleRepository by inject()

    override suspend fun doWork(): Result {
        val routines = routineRepository.routinesFlow.first()
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        scheduleRepository.reconcile(routines = routines, now = now)
        // reconcile() is a no-op whenever a routine's signature hasn't changed, so it alone can't
        // repair an ongoing notification stuck on an elapsed task because of a missed alarm.
        // refreshNotifications() always resyncs it against the real current task, unconditionally.
        scheduleRepository.refreshNotifications(routines = routines, now = now)
        return Result.success()
    }
}
