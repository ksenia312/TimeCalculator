package com.example.morningcalculator.app.schedule

import com.example.morningcalculator.domain.repository.RoutineAlarmGateway
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.RoutineScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Instant

class RoutineScheduleInitializer(
    private val routineRepository: RoutineRepository,
    private val scheduleRepository: RoutineScheduleRepository,
    private val alarmGateway: RoutineAlarmGateway,
    private val permissionRequester: RoutineExactAlarmPermissionRequester,
) {
    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(FlowPreview::class)
    fun start() {
        if (!started.compareAndSet(false, true)) return
        scope.launch {
            routineRepository.routinesFlow
                .debounce(300)
                .collect { routines ->
                    if (!alarmGateway.canScheduleExactAlarms() && routines.any { it.data.isNotEmpty() }) {
                        permissionRequester.promptIfNeeded()
                    }
                    scheduleRepository.reconcile(
                        routines = routines,
                        now = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                    )
                }
        }
    }
}
