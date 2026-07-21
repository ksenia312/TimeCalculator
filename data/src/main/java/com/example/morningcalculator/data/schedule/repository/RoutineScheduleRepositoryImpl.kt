package com.example.morningcalculator.data.schedule.repository

import com.example.morningcalculator.data.schedule.computation.calculateSchedule
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineAlarmKind
import com.example.morningcalculator.domain.model.RoutineSchedule
import com.example.morningcalculator.domain.model.RoutineSchedulePhase
import com.example.morningcalculator.domain.model.ScheduleRecord
import com.example.morningcalculator.domain.repository.RoutineAlarmGateway
import com.example.morningcalculator.domain.repository.RoutineNotificationGateway
import com.example.morningcalculator.domain.repository.RoutineScheduleRepository
import com.example.morningcalculator.domain.repository.ScheduleRecordDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant

class RoutineScheduleRepositoryImpl(
    private val alarmGateway: RoutineAlarmGateway,
    private val notificationGateway: RoutineNotificationGateway,
    private val scheduleRecordDataSource: ScheduleRecordDataSource,
) : RoutineScheduleRepository {

    private val mutex = Mutex()

    override fun computeSchedule(
        routine: Routine,
        now: Instant,
    ): RoutineSchedule {
        return calculateSchedule(routine)
    }

    override suspend fun reconcile(
        routines: List<Routine>,
        now: Instant,
        forceReschedule: Boolean,
    ) {
        mutex.withLock {
            val currentIds = routines.map { it.id }.toSet()
            val removedIds = scheduleRecordDataSource.trackedRoutineIds().minus(currentIds)
            removedIds.forEach { routineId ->
                val record = scheduleRecordDataSource.getRecord(routineId)
                alarmGateway.cancelRoutine(routineId, record?.taskCount ?: 0)
                notificationGateway.cancelRoutineNotifications(routineId)
                scheduleRecordDataSource.removeRecord(routineId)
            }

            routines.forEach { routine ->
                val schedule = computeSchedule(routine, now)
                val record = scheduleRecordDataSource.getRecord(routine.id)
                if (!forceReschedule && record?.signature == schedule.signature) {
                    return@forEach
                }

                if (record != null) {
                    alarmGateway.cancelRoutine(routine.id, record.taskCount)
                } else {
                    alarmGateway.cancelRoutine(routine.id, schedule.tasks.size)
                }

                when (schedule.phaseAt(now)) {
                    RoutineSchedulePhase.FUTURE -> {
                        alarmGateway.schedule(schedule)
                        notificationGateway.cancelProgress(routine.id)
                    }

                    RoutineSchedulePhase.ACTIVE -> {
                        alarmGateway.schedule(schedule)
                        notificationGateway.postProgress(routine, schedule, now)
                    }

                    RoutineSchedulePhase.FINISHED -> {
                        notificationGateway.cancelRoutineNotifications(routine.id)
                        scheduleRecordDataSource.removeRecord(routine.id)
                        return@forEach
                    }
                }

                scheduleRecordDataSource.putRecord(
                    routine.id,
                    ScheduleRecord(
                        signature = schedule.signature,
                        taskCount = schedule.tasks.size,
                    ),
                )
            }
        }
    }

    override suspend fun handleAlarm(
        routine: Routine,
        kind: RoutineAlarmKind,
        boundaryIndex: Int,
        triggerAtMillis: Long,
        now: Instant,
    ) {
        val schedule = computeSchedule(routine, now)
        val expectedTrigger = when (kind) {
            RoutineAlarmKind.START -> schedule.effectiveStart.toEpochMilliseconds()
            RoutineAlarmKind.TASK -> schedule.tasks.getOrNull(boundaryIndex)?.start?.toEpochMilliseconds()
            RoutineAlarmKind.END -> schedule.end.toEpochMilliseconds()
        } ?: return

        if (triggerAtMillis >= 0 && triggerAtMillis != expectedTrigger) return

        when (kind) {
            RoutineAlarmKind.START -> {
                notificationGateway.postProgress(routine, schedule, now)
            }

            RoutineAlarmKind.TASK -> {
                notificationGateway.postProgress(routine, schedule, now)
            }

            RoutineAlarmKind.END -> {
                notificationGateway.cancelProgress(routine.id)
                scheduleRecordDataSource.removeRecord(routine.id)
            }
        }
    }
}
