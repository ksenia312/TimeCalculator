package com.xenikii.timecalculator.data.schedule.repository

import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineAlarmKind
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.RoutineRecurrence
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.model.RoutineScheduleAnchor
import com.xenikii.timecalculator.domain.model.ScheduleRecord
import com.xenikii.timecalculator.domain.model.SubData
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.domain.repository.RoutineAlarmGateway
import com.xenikii.timecalculator.domain.repository.RoutineNotificationGateway
import com.xenikii.timecalculator.domain.repository.ScheduleRecordDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class RoutineScheduleRepositoryImplTest {

    @Test
    fun `computes five minute START schedule`() {
        val repository = createRepository()
        val now = instant(hour = 9)
        val routine = routine(
            scheduledAt = now,
            anchor = RoutineScheduleAnchor.START,
        )

        val schedule = repository.computeSchedule(routine, now)

        assertEquals(now, schedule.effectiveStart)
        assertEquals(now + 5.minutes, schedule.end)
        assertEquals(now, schedule.tasks.single().start)
        assertEquals(now + 5.minutes, schedule.tasks.single().end)
    }

    @Test
    fun `computes five minute END schedule`() {
        val repository = createRepository()
        val now = instant(hour = 9)
        val routine = routine(
            scheduledAt = now,
            anchor = RoutineScheduleAnchor.END,
        )

        val schedule = repository.computeSchedule(routine, now)

        assertEquals(now - 5.minutes, schedule.effectiveStart)
        assertEquals(now, schedule.end)
    }

    @Test
    fun `computes every 2 days next cycle`() {
        val repository = createRepository()
        val now = instant(day = 5, hour = 9, minute = 10)
        val routine = routine(
            scheduledAt = instant(day = 1, hour = 9),
            recurrence = RoutineRecurrence(interval = 2, unit = RoutineRecurrenceUnit.DAY),
        )

        val schedule = repository.computeSchedule(routine, now)

        assertEquals(instant(day = 7, hour = 9), schedule.effectiveStart)
        assertEquals(instant(day = 7, hour = 9, minute = 5), schedule.end)
    }

    @Test
    fun `computes every week next cycle`() {
        val repository = createRepository()
        val now = instant(day = 10, hour = 10)
        val routine = routine(
            scheduledAt = instant(day = 3, hour = 9),
            recurrence = RoutineRecurrence(interval = 1, unit = RoutineRecurrenceUnit.WEEK),
        )

        val schedule = repository.computeSchedule(routine, now)

        assertEquals(instant(day = 17, hour = 9), schedule.effectiveStart)
    }

    @Test
    fun `computes every month with calendar shift`() {
        val repository = createRepository()
        val now = zonedInstant(2026, 2, 15, 10, 0)
        val routine = routine(
            scheduledAt = zonedInstant(2026, 1, 31, 9, 0),
            recurrence = RoutineRecurrence(interval = 1, unit = RoutineRecurrenceUnit.MONTH),
        )

        val schedule = repository.computeSchedule(routine, now)

        assertEquals(zonedInstant(2026, 2, 28, 9, 0), schedule.effectiveStart)
    }

    @Test
    fun `computes every year cycle`() {
        val repository = createRepository()
        val now = zonedInstant(2027, 7, 2, 10, 0)
        val routine = routine(
            scheduledAt = zonedInstant(2025, 7, 1, 9, 0),
            recurrence = RoutineRecurrence(interval = 1, unit = RoutineRecurrenceUnit.YEAR),
        )

        val schedule = repository.computeSchedule(routine, now)

        assertEquals(zonedInstant(2028, 7, 1, 9, 0), schedule.effectiveStart)
    }

    @Test
    fun `reschedules recurring routine after END alarm`() {
        runBlocking {
        val alarmGateway = RecordingAlarmGateway()
        val notificationGateway = RecordingNotificationGateway()
        val records = InMemoryScheduleRecordDataSource()
        val repository = RoutineScheduleRepositoryImpl(
            alarmGateway = alarmGateway,
            notificationGateway = notificationGateway,
            scheduleRecordDataSource = records,
        )
        val routine = routine(
            scheduledAt = instant(day = 1, hour = 9),
            recurrence = RoutineRecurrence(interval = 1, unit = RoutineRecurrenceUnit.DAY),
        )
        val initialNow = instant(day = 1, hour = 8)
        repository.reconcile(routines = listOf(routine), now = initialNow)
        val initial = alarmGateway.scheduledPlans.last()

        val endNow = initial.end
        repository.handleAlarm(
            routine = routine,
            kind = RoutineAlarmKind.END,
            boundaryIndex = -1,
            triggerAtMillis = endNow.toEpochMilliseconds(),
            now = endNow,
        )

        val rescheduled = alarmGateway.scheduledPlans.last()
        assertTrue(rescheduled.effectiveStart > initial.effectiveStart)
        assertNotNull(records.getRecord(routine.id))
        assertTrue(notificationGateway.cancelProgressCalls.contains(routine.id))
        }
    }

    private fun createRepository(): RoutineScheduleRepositoryImpl =
        RoutineScheduleRepositoryImpl(
            alarmGateway = RecordingAlarmGateway(),
            notificationGateway = RecordingNotificationGateway(),
            scheduleRecordDataSource = InMemoryScheduleRecordDataSource(),
        )

    private fun routine(
        scheduledAt: Instant,
        anchor: RoutineScheduleAnchor = RoutineScheduleAnchor.START,
        recurrence: RoutineRecurrence = RoutineRecurrence(),
    ): Routine {
        val task = Task(
            id = "task-1",
            title = "Task",
            description = "",
            data = listOf(SubData(id = "sub-5", duration = 5.minutes)),
        )
        return Routine(
            id = "routine-1",
            title = "Routine",
            scheduledAt = scheduledAt,
            scheduledAtAnchor = anchor,
            recurrence = recurrence,
            modifiedAt = 0L,
            color = "#000000",
            data = listOf(RoutineLink(id = "link-1", task = task, subData = task.data.first())),
        )
    }

    private fun instant(day: Int = 1, hour: Int, minute: Int = 0): Instant {
        val epochMillis = ((day - 1) * 24L * 60L * 60L + hour * 60L * 60L + minute * 60L) * 1000L
        return Instant.fromEpochMilliseconds(epochMillis)
    }

    private fun zoned(year: Int, month: Int, day: Int, hour: Int, minute: Int): ZonedDateTime =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.systemDefault())

    private fun zonedInstant(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant =
        Instant.fromEpochMilliseconds(zoned(year, month, day, hour, minute).toInstant().toEpochMilli())
}

private class RecordingAlarmGateway : RoutineAlarmGateway {
    val scheduledPlans = mutableListOf<RoutineSchedule>()
    override fun canScheduleExactAlarms(): Boolean = true
    override fun cancelRoutine(routineId: String, taskCount: Int) = Unit
    override fun schedule(plan: RoutineSchedule) {
        scheduledPlans += plan
    }
}

private class RecordingNotificationGateway : RoutineNotificationGateway {
    val cancelProgressCalls = mutableListOf<String>()
    override fun cancelRoutineNotifications(routineId: String) = Unit
    override fun cancelProgress(routineId: String) {
        cancelProgressCalls += routineId
    }

    override fun postProgress(routine: Routine, plan: RoutineSchedule, now: Instant) = Unit
}

private class InMemoryScheduleRecordDataSource : ScheduleRecordDataSource {
    private val records = linkedMapOf<String, ScheduleRecord>()
    override fun getRecord(routineId: String) = records[routineId]
    override fun putRecord(routineId: String, record: ScheduleRecord) {
        records[routineId] = record
    }

    override fun removeRecord(routineId: String) {
        records.remove(routineId)
    }

    override fun trackedRoutineIds(): Set<String> = records.keys
}
