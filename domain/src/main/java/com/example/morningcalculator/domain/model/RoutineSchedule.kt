package com.example.morningcalculator.domain.model

import kotlin.time.Duration
import kotlin.time.Instant

data class RoutineSchedule(
    val routineId: String,
    val routineTitle: String,
    val effectiveStart: Instant,
    val end: Instant,
    val totalDuration: Duration,
    val tasks: List<ScheduledTask>,
    val signature: String,
) {
    fun phaseAt(now: Instant): RoutineSchedulePhase = when {
        now < effectiveStart -> RoutineSchedulePhase.FUTURE
        now >= end -> RoutineSchedulePhase.FINISHED
        else -> RoutineSchedulePhase.ACTIVE
    }

    fun taskAt(now: Instant): ScheduledTask? = tasks.firstOrNull { it.contains(now) }

    fun taskIndexAt(now: Instant): Int? = taskAt(now)?.index
}

data class ScheduledTask(
    val index: Int,
    val title: String,
    val start: Instant,
    val end: Instant,
    val duration: Duration,
) {
    fun contains(now: Instant): Boolean {
        return when {
            duration <= Duration.ZERO -> now == start
            else -> now in start..<end
        }
    }
}

enum class RoutineSchedulePhase {
    FUTURE,
    ACTIVE,
    FINISHED,
}

enum class RoutineAlarmKind {
    START,
    TASK,
    END,
}

data class ScheduleRecord(
    val signature: String,
    val taskCount: Int,
)
