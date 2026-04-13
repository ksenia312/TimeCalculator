package com.example.morningcalculator.shared.extensions

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineScheduleAnchor
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.ZoneId
import java.util.TimeZone
import kotlin.time.Duration
import kotlin.time.Instant

fun Routine.isOngoing(): Boolean {
    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    return now in startAtInstant()..endAtInstant()
}

fun Routine.isCompleted(): Boolean {
    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    return now > endAtInstant()
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
    return startAtInstant().toDeviceLocalDateTime()
}

fun Routine.endAt(): LocalDateTime {
    return endAtInstant().toDeviceLocalDateTime()
}

fun Routine.timeOnMoment(index: Int): LocalDateTime {
    val safeIndex = index.coerceAtLeast(-1)
    val offset: Duration = data.foldIndexed(Duration.ZERO) { currentIndex, acc, link ->
        if (currentIndex > safeIndex) return@foldIndexed acc
        acc + (link.subData?.duration ?: taskDuration(link.task))
    }

    return (startAtInstant() + offset).toDeviceLocalDateTime()
}

private fun Routine.totalDuration(): Duration {
    return data.fold(Duration.ZERO) { acc, link ->
        acc + (link.subData?.duration ?: taskDuration(link.task))
    }
}

private fun taskDuration(task: com.example.morningcalculator.core.model.Task): Duration {
    return task.data.fold(Duration.ZERO) { acc, subData ->
        acc + subData.duration
    }
}

private fun Instant.toDeviceLocalDateTime(): LocalDateTime {
    val millis = toEpochMilliseconds()
    return java.time.Instant
        .ofEpochMilli(millis)
        .atZone(deviceZoneId())
        .toLocalDateTime()
        .toKotlinLocalDateTime()
}

fun deviceZoneId(): ZoneId {
    return ZoneId.of(TimeZone.getDefault().id)
}