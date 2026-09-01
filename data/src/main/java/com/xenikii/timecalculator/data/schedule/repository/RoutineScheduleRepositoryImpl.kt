package com.xenikii.timecalculator.data.schedule.repository

import com.xenikii.timecalculator.data.schedule.computation.calculateSchedule
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineAlarmKind
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.model.RoutineSchedulePhase
import com.xenikii.timecalculator.domain.model.ScheduleRecord
import com.xenikii.timecalculator.domain.repository.NotificationSettingsLocalDataSource
import com.xenikii.timecalculator.domain.repository.RoutineAlarmGateway
import com.xenikii.timecalculator.domain.repository.RoutineNotificationGateway
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import com.xenikii.timecalculator.domain.repository.ScheduleRecordDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant

class RoutineScheduleRepositoryImpl(
    private val alarmGateway: RoutineAlarmGateway,
    private val notificationGateway: RoutineNotificationGateway,
    private val scheduleRecordDataSource: ScheduleRecordDataSource,
    private val notificationSettings: NotificationSettingsLocalDataSource,
) : RoutineScheduleRepository {

    private val mutex = Mutex()

    override fun computeSchedule(
        routine: Routine,
        now: Instant,
    ): RoutineSchedule {
        return calculateSchedule(routine, now)
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
                rescheduleRoutine(routine = routine, now = now, forceReschedule = forceReschedule)
            }
        }
    }

    override suspend fun refreshNotifications(
        routines: List<Routine>,
        now: Instant,
    ) {
        mutex.withLock {
            val enabled = notificationSettings.isEnabled()
            routines.forEach { routine ->
                val schedule = computeSchedule(routine, now)
                val isActive = schedule.phaseAt(now) == RoutineSchedulePhase.ACTIVE
                if (enabled && isActive) {
                    notificationGateway.postProgress(routine, schedule, now, alert = false)
                } else {
                    notificationGateway.cancelProgress(routine.id)
                }
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
        mutex.withLock {
            val schedule = if (
                kind == RoutineAlarmKind.END &&
                routine.recurrence.unit != RoutineRecurrenceUnit.NONE
            ) {
                val referenceMillis = if (triggerAtMillis >= 0L) {
                    triggerAtMillis - 1L
                } else {
                    now.toEpochMilliseconds() - 1L
                }
                computeSchedule(routine, Instant.fromEpochMilliseconds(referenceMillis))
            } else {
                computeSchedule(routine, now)
            }
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
                    if (routine.recurrence.unit != RoutineRecurrenceUnit.NONE) {
                        rescheduleRoutine(routine = routine, now = now, forceReschedule = true)
                    }
                }
            }
        }
    }

    private fun rescheduleRoutine(
        routine: Routine,
        now: Instant,
        forceReschedule: Boolean,
    ) {
        val schedule = computeSchedule(routine, now)
        val record = scheduleRecordDataSource.getRecord(routine.id)
        if (!forceReschedule && record?.signature == schedule.signature) return

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
                return
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
