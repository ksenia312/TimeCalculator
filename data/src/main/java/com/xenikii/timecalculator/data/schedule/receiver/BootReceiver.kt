package com.xenikii.timecalculator.data.schedule.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xenikii.timecalculator.data.schedule.watchdog.RoutineScheduleWatchdogScheduler
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val routineRepository: RoutineRepository by inject()
    private val scheduleRepository: RoutineScheduleRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        RoutineScheduleWatchdogScheduler.ensureScheduled(context)
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val routines = routineRepository.routinesFlow.first()
                scheduleRepository.reconcile(
                    routines = routines,
                    now = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                    forceReschedule = true,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
