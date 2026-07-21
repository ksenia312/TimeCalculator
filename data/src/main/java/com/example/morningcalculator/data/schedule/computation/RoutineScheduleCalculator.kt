package com.example.morningcalculator.data.schedule.computation

import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineLink
import com.example.morningcalculator.domain.model.RoutineSchedule
import com.example.morningcalculator.domain.model.RoutineScheduleAnchor
import com.example.morningcalculator.domain.model.ScheduledTask
import kotlin.time.Duration
import kotlin.time.Instant

internal fun calculateSchedule(routine: Routine): RoutineSchedule {
    val durations = routine.data.map { linkDuration(it) }
    val totalDuration = durations.fold(Duration.ZERO) { acc, duration -> acc + duration }
    val effectiveStart = when (routine.scheduledAtAnchor) {
        RoutineScheduleAnchor.START -> routine.scheduledAt
        RoutineScheduleAnchor.END -> routine.scheduledAt - totalDuration
    }
    val end = effectiveStart + totalDuration

    var cursor = effectiveStart
    val tasks = routine.data.mapIndexed { index, link ->
        val duration = durations.getOrElse(index) { Duration.ZERO }
        val taskEnd = cursor + duration
        ScheduledTask(
            index = index,
            title = link.task.title,
            start = cursor,
            end = taskEnd,
            duration = duration,
        ).also {
            cursor = taskEnd
        }
    }

    return RoutineSchedule(
        routineId = routine.id,
        routineTitle = routine.title,
        effectiveStart = effectiveStart,
        end = end,
        totalDuration = totalDuration,
        tasks = tasks,
        signature = buildSignature(routine.title, effectiveStart, end, tasks),
    )
}

internal fun linkDuration(link: RoutineLink): Duration {
    val selected = link.subData?.duration
    if (selected != null) return selected

    val fallback = link.task.dataSortedByDuration.firstOrNull()?.duration
    return fallback ?: Duration.ZERO
}

private fun buildSignature(
    routineTitle: String,
    effectiveStart: Instant,
    end: Instant,
    tasks: List<ScheduledTask>,
): String = buildString {
    append(routineTitle)
    append('|')
    append(effectiveStart.toEpochMilliseconds())
    append('|')
    append(end.toEpochMilliseconds())
    append('|')
    tasks.forEach { task ->
        append(task.index)
        append(':')
        append(task.title)
        append(':')
        append(task.start.toEpochMilliseconds())
        append(':')
        append(task.end.toEpochMilliseconds())
        append(':')
        append(task.duration.inWholeMilliseconds)
        append('|')
    }
}
