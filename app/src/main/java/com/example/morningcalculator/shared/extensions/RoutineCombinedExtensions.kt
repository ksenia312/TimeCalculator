package com.example.morningcalculator.shared.extensions

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineScheduleAnchor
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.toJavaDuration

fun Routine.isOngoing(): Boolean {
    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    return now in scheduledAt..endAt().toInstant(UtcOffset.ZERO)
}

fun Routine.isCompleted(): Boolean {
    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    return now > endAt().toInstant(UtcOffset.ZERO)
}

fun Routine.startAtInstant(): Instant {
    val total = totalDuration()
    return when (scheduledAtAnchor) {
        RoutineScheduleAnchor.START -> scheduledAt
        RoutineScheduleAnchor.END -> scheduledAt - total
    }
}

fun Routine.endAtInstant(): Instant {
    val total = totalDuration()
    return when (scheduledAtAnchor) {
        RoutineScheduleAnchor.START -> scheduledAt + total
        RoutineScheduleAnchor.END -> scheduledAt
    }
}

fun Routine.whenToStart(): LocalDateTime {
    val startMillis = startAtInstant().toEpochMilliseconds()
    return java.time.Instant
        .ofEpochMilli(startMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .toKotlinLocalDateTime()
}

fun Routine.endAt(): LocalDateTime {
    val endMillis = endAtInstant().toEpochMilliseconds()
    return java.time.Instant
        .ofEpochMilli(endMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .toKotlinLocalDateTime()
}

fun Routine.timeOnMoment(index: Int): LocalDateTime {
    val safeIndex = index.coerceAtLeast(-1)
    val offset: Duration = data.foldIndexed(Duration.ZERO) { currentIndex, acc, link ->
        if (currentIndex > safeIndex) return@foldIndexed acc
        acc + (link.subData?.duration ?: Duration.ZERO)
    }

    val startMillis = startAtInstant().toEpochMilliseconds()
    val start = java.time.Instant
        .ofEpochMilli(startMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    return start.plus(offset.toJavaDuration()).toKotlinLocalDateTime()
}

private fun Routine.totalDuration(): Duration {
    return data.fold(Duration.ZERO) { acc, link ->
        acc + (link.subData?.duration ?: Duration.ZERO)
    }
}