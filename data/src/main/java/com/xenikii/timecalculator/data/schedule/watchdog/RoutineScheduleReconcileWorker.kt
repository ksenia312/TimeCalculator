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
        scheduleRepository.reconcile(
            routines = routines,
            now = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
        )
        return Result.success()
    }
}
