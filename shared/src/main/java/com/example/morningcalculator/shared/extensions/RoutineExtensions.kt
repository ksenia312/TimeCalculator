package com.example.morningcalculator.shared.extensions

import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineLink
import com.example.morningcalculator.domain.model.RoutineScheduleAnchor
import kotlin.time.Duration

fun Routine.getCurrentTaskIndex(): Int? {
    val nowMillis = System.currentTimeMillis()
    val durations = data.map { it.effectiveDuration() }
    val totalDuration = durations.fold(Duration.ZERO) { acc, d -> acc + d }

    val scheduledAtMillis = (scheduledAt.toEpochMilliseconds() / 60_000L) * 60_000L
    val startMillis = when (scheduledAtAnchor) {
        RoutineScheduleAnchor.START -> scheduledAtMillis
        RoutineScheduleAnchor.END -> scheduledAtMillis - totalDuration.inWholeMilliseconds
    }

    val endMillis = startMillis + totalDuration.inWholeMilliseconds

    if (nowMillis !in startMillis..<endMillis) {
        return null
    }

    var cursor = startMillis
    durations.forEachIndexed { index, duration ->
        if (nowMillis >= cursor && nowMillis < cursor + duration.inWholeMilliseconds) {
            return index
        }
        cursor += duration.inWholeMilliseconds
    }

    return null
}

fun RoutineLink.effectiveDuration(): Duration {
    val direct = subData?.duration
    if (direct != null) return direct

    val fallback = task.dataSortedByDuration.firstOrNull()?.duration
    return fallback ?: Duration.ZERO
}
