package com.xenikii.timecalculator.domain.repository

import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineAlarmKind
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.model.ScheduleRecord
import kotlin.time.Instant

interface RoutineScheduleRepository {
    fun computeSchedule(
        routine: Routine,
        now: Instant,
    ): RoutineSchedule

    suspend fun reconcile(
        routines: List<Routine>,
        now: Instant,
        forceReschedule: Boolean = false,
    )

    suspend fun handleAlarm(
        routine: Routine,
        kind: RoutineAlarmKind,
        boundaryIndex: Int,
        triggerAtMillis: Long,
        now: Instant,
    )
}

interface RoutineAlarmGateway {
    fun canScheduleExactAlarms(): Boolean
    fun cancelRoutine(routineId: String, taskCount: Int)
    fun schedule(plan: RoutineSchedule)
}

interface RoutineNotificationGateway {
    fun cancelRoutineNotifications(routineId: String)
    fun cancelProgress(routineId: String)
    fun postProgress(routine: Routine, plan: RoutineSchedule, now: Instant)
}

interface ScheduleRecordDataSource {
    fun getRecord(routineId: String): ScheduleRecord?
    fun putRecord(routineId: String, record: ScheduleRecord)
    fun removeRecord(routineId: String)
    fun trackedRoutineIds(): Set<String>
}

interface RoutineScheduleInitializer {
    fun start()
}
