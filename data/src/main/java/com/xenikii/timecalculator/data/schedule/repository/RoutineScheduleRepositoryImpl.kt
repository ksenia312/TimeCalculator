package com.xenikii.timecalculator.data.schedule.repository

import com.xenikii.timecalculator.data.schedule.computation.calculateSchedule
import com.xenikii.timecalculator.domain.model.NotificationMode
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineAlarmKind
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.model.RoutineSchedulePhase
import com.xenikii.timecalculator.domain.model.ScheduleRecord
import com.xenikii.timecalculator.domain.model.effectiveNotificationMode
import com.xenikii.timecalculator.domain.repository.NotificationSettingsLocalDataSource
import com.xenikii.timecalculator.domain.repository.PremiumRepository
import com.xenikii.timecalculator.domain.repository.RoutineAlarmGateway
import com.xenikii.timecalculator.domain.repository.RoutineNotificationGateway
import com.xenikii.timecalculator.domain.repository.RoutineRepository
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
    private val premiumRepository: PremiumRepository,
    private val routineRepository: RoutineRepository,
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
            // A routine not ACTIVE (paused, manually or automatically) is treated exactly like a
            // deleted one for scheduling purposes: it drops out of currentIds below, so its
            // already-armed alarms/notifications get cancelled by the same cleanup that handles
            // actual deletions, instead of only skipping future (re)scheduling.
            val activeRoutines = routines.filter { it.isActive }
            val currentIds = activeRoutines.map { it.id }.toSet()
            val removedIds = scheduleRecordDataSource.trackedRoutineIds().minus(currentIds)
            removedIds.forEach { routineId ->
                val record = scheduleRecordDataSource.getRecord(routineId)
                alarmGateway.cancelRoutine(routineId, record?.taskCount ?: 0)
                notificationGateway.cancelRoutineNotifications(routineId)
                scheduleRecordDataSource.removeRecord(routineId)
            }

            activeRoutines.forEach { routine ->
                rescheduleRoutine(routine = routine, now = now, forceReschedule = forceReschedule)
            }
        }
    }

    override suspend fun refreshNotifications(
        routines: List<Routine>,
        now: Instant,
    ) {
        mutex.withLock {
            routines.filter { it.isActive }.forEach { routine ->
                syncNotification(routine, computeSchedule(routine, now), now)
            }
        }
    }

    /**
     * Resyncs the ongoing notification with the ground truth of `schedule`, regardless of what
     * (if anything) is currently showing. This is the self-healing path: it must be safe to call
     * whenever we're not sure the notification reflects the current task, e.g. after a missed or
     * stale alarm, so a stuck notification is never left ticking down on an elapsed task forever.
     */
    private fun syncNotification(routine: Routine, schedule: RoutineSchedule, now: Instant) {
        val enabled = notificationSettings.isEnabled()
        val isActive = schedule.phaseAt(now) == RoutineSchedulePhase.ACTIVE
        if (enabled && isActive) {
            val mode = pinNotificationMode(routine.id, schedule)
            notificationGateway.postProgress(routine, schedule, now, mode = mode, alert = false)
        } else {
            notificationGateway.cancelProgress(routine.id)
        }
    }

    /** What a routine starting right now should be pinned to: EVERY_TASK downgrades to
     * START_AND_END while not premium. Only used to establish a new pin - an already-active
     * routine keeps whatever [pinNotificationMode] gave it when it started. `?: true`: premium
     * not confirmed yet reads as "don't downgrade", not as free. */
    private fun currentDesiredMode(): NotificationMode =
        notificationSettings.getMode().effectiveNotificationMode(premiumRepository.isPremiumCached() ?: true)

    /**
     * Ensures `routineId`'s schedule record carries a notification mode, without ever changing
     * one that's already set - that's the pin: whatever mode a routine started under governs it
     * for its whole run, immune to premium status changing mid-routine. Safe to call repeatedly
     * (resyncs, drift correction) since it's a no-op once a mode is set.
     */
    private fun pinNotificationMode(routineId: String, schedule: RoutineSchedule): NotificationMode {
        val record = scheduleRecordDataSource.getRecord(routineId)
        val mode = record?.notificationMode ?: currentDesiredMode()
        scheduleRecordDataSource.putRecord(
            routineId,
            ScheduleRecord(signature = schedule.signature, taskCount = schedule.tasks.size, notificationMode = mode),
        )
        return mode
    }

    override suspend fun handleAlarm(
        routine: Routine,
        kind: RoutineAlarmKind,
        boundaryIndex: Int,
        triggerAtMillis: Long,
        now: Instant,
    ) {
        mutex.withLock {
            // Paused routines never get alarms armed (see reconcile above), but a race between a
            // pause taking effect and an already in-flight alarm is possible - drop it rather
            // than reschedule/notify for a routine that shouldn't be running.
            if (!routine.isActive) return@withLock

            // The routine's alarm is genuinely firing right now, for any kind (START/TASK/END) -
            // this is the one and only place that counts as "the routine triggered". Bookkeeping,
            // not a user edit: see recordRoutineTriggered's contract.
            routineRepository.recordRoutineTriggered(routine.id, now)

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

            if (triggerAtMillis >= 0 && triggerAtMillis != expectedTrigger) {
                // The schedule shifted since this alarm was armed (routine edited, recurrence
                // rolled over, etc). Don't just drop the alarm silently: resync the notification
                // to whatever is actually true right now instead of leaving a stale one behind.
                syncNotification(routine, schedule, now)
                return
            }

            when (kind) {
                RoutineAlarmKind.START -> {
                    // The routine is starting right now: this is where its notification mode gets
                    // pinned for its whole run (see pinNotificationMode). Whichever of these two
                    // actually shows something is decided by that mode inside the gateway.
                    // alertTask pins the alert to the task this alarm was armed for (index 0), so a
                    // delayed delivery can't relabel it with whatever task the wall clock has since
                    // moved on to.
                    val mode = pinNotificationMode(routine.id, schedule)
                    notificationGateway.postProgress(
                        routine,
                        schedule,
                        now,
                        mode = mode,
                        alertTask = schedule.tasks.firstOrNull(),
                    )
                    notificationGateway.postRoutineStarted(routine, mode = mode)
                }

                RoutineAlarmKind.TASK -> {
                    // Read-only: the routine is already under way, so this reuses whatever mode
                    // was pinned at RoutineAlarmKind.START rather than re-deciding it.
                    val mode = scheduleRecordDataSource.getRecord(routine.id)?.notificationMode ?: currentDesiredMode()
                    notificationGateway.postProgress(
                        routine,
                        schedule,
                        now,
                        mode = mode,
                        alertTask = schedule.tasks.getOrNull(boundaryIndex),
                    )
                }

                RoutineAlarmKind.END -> {
                    // Clears the progress notification *and* the last task-started alert, which
                    // would otherwise never get replaced (there's no next task to post over it)
                    // and would linger after the routine finished.
                    notificationGateway.cancelRoutineNotifications(routine.id)
                    notificationGateway.postRoutineFinished(routine)
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
                // Not started yet, so no mode to pin - notificationMode stays null until it is.
                scheduleRecordDataSource.putRecord(
                    routine.id,
                    ScheduleRecord(signature = schedule.signature, taskCount = schedule.tasks.size),
                )
            }

            RoutineSchedulePhase.ACTIVE -> {
                alarmGateway.schedule(schedule)
                // Reschedule of a routine already under way (edited mid-run, or discovered active
                // on reconcile) - pinNotificationMode reuses its existing pin rather than
                // re-deciding it, and also (re)writes signature/taskCount to match.
                val mode = pinNotificationMode(routine.id, schedule)
                notificationGateway.postProgress(routine, schedule, now, mode = mode)
            }

            RoutineSchedulePhase.FINISHED -> {
                notificationGateway.cancelRoutineNotifications(routine.id)
                scheduleRecordDataSource.removeRecord(routine.id)
            }
        }
    }
}
