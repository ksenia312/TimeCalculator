package com.xenikii.timecalculator.data.schedule.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xenikii.timecalculator.data.schedule.alarm.EXTRA_ALARM_KIND
import com.xenikii.timecalculator.data.schedule.alarm.EXTRA_BOUNDARY_INDEX
import com.xenikii.timecalculator.data.schedule.alarm.EXTRA_ROUTINE_ID
import com.xenikii.timecalculator.data.schedule.alarm.EXTRA_TRIGGER_AT_MILLIS
import com.xenikii.timecalculator.domain.model.RoutineAlarmKind
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

class RoutineAlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val routineRepository: RoutineRepository by inject()
    private val scheduleRepository: RoutineScheduleRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                handleIntent(intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleIntent(intent: Intent) {
        val routineId = intent.getStringExtra(EXTRA_ROUTINE_ID) ?: return
        val kind = intent.getStringExtra(EXTRA_ALARM_KIND)?.let {
            runCatching { RoutineAlarmKind.valueOf(it) }.getOrNull()
        } ?: return
        val boundaryIndex = intent.getIntExtra(EXTRA_BOUNDARY_INDEX, -1)
        val triggerAtMillis = intent.getLongExtra(EXTRA_TRIGGER_AT_MILLIS, -1L)
        val routine = routineRepository.getRoutineFlow(routineId).first() ?: return
        scheduleRepository.handleAlarm(
            routine = routine,
            kind = kind,
            boundaryIndex = boundaryIndex,
            triggerAtMillis = triggerAtMillis,
            now = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
        )
    }
}
