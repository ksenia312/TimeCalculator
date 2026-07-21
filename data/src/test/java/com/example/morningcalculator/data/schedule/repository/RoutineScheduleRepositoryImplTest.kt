package com.example.morningcalculator.data.schedule.repository

import com.example.morningcalculator.data.schedule.repository.RoutineScheduleRepositoryImpl
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineLink
import com.example.morningcalculator.domain.model.RoutineSchedule
import com.example.morningcalculator.domain.model.RoutineScheduleAnchor
import com.example.morningcalculator.domain.model.ScheduleRecord
import com.example.morningcalculator.domain.model.SubData
import com.example.morningcalculator.domain.model.Task
import com.example.morningcalculator.domain.repository.RoutineAlarmGateway
import com.example.morningcalculator.domain.repository.RoutineNotificationGateway
import com.example.morningcalculator.domain.repository.ScheduleRecordDataSource
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class RoutineScheduleRepositoryImplTest {

    private val repository = RoutineScheduleRepositoryImpl(
        alarmGateway = NoopAlarmGateway,
        notificationGateway = NoopNotificationGateway,
        scheduleRecordDataSource = InMemoryScheduleRecordDataSource(),
    )

    @Test
    fun `computes five minute START schedule`() {
        val routine = routine(RoutineScheduleAnchor.START)

        val schedule = repository.computeSchedule(routine, now())

        assertEquals(now(), schedule.effectiveStart)
        assertEquals(now() + 5.minutes, schedule.end)
        assertEquals(5.minutes, schedule.totalDuration)
        assertEquals(now(), schedule.tasks.single().start)
        assertEquals(now() + 5.minutes, schedule.tasks.single().end)
    }

    @Test
    fun `computes five minute END schedule`() {
        val routine = routine(RoutineScheduleAnchor.END)

        val schedule = repository.computeSchedule(routine, now())

        assertEquals(now() - 5.minutes, schedule.effectiveStart)
        assertEquals(now(), schedule.end)
        assertEquals(5.minutes, schedule.totalDuration)
        assertEquals(now() - 5.minutes, schedule.tasks.single().start)
        assertEquals(now(), schedule.tasks.single().end)
    }

    private fun routine(anchor: RoutineScheduleAnchor): Routine {
        val task = Task(
            id = "task-1",
            title = "Task",
            description = "",
            data = listOf(SubData(id = "sub-5", duration = 5.minutes)),
        )
        return Routine(
            id = "routine-1",
            title = "Routine",
            scheduledAt = now(),
            scheduledAtAnchor = anchor,
            modifiedAt = 0L,
            color = "#000000",
            data = listOf(RoutineLink(id = "link-1", task = task, subData = task.data.first())),
        )
    }

    private fun now(): Instant = Instant.fromEpochMilliseconds(9L * 60L * 60L * 1000L)
}

private object NoopAlarmGateway : RoutineAlarmGateway {
    override fun canScheduleExactAlarms(): Boolean = true
    override fun cancelRoutine(routineId: String, taskCount: Int) = Unit
    override fun schedule(plan: RoutineSchedule) = Unit
}

private object NoopNotificationGateway : RoutineNotificationGateway {
    override fun cancelRoutineNotifications(routineId: String) = Unit
    override fun cancelProgress(routineId: String) = Unit
    override fun postProgress(routine: Routine, plan: RoutineSchedule, now: Instant) = Unit
    override fun postRoutineStartAlert(routine: Routine, plan: RoutineSchedule) = Unit
    override fun postTaskAlert(routine: Routine, plan: RoutineSchedule, boundaryIndex: Int) = Unit
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
