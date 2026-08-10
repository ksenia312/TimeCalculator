package com.xenikii.timecalculator.data.schedule.computation

import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.model.RoutineScheduleAnchor
import com.xenikii.timecalculator.domain.model.ScheduledTask
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Instant

internal fun calculateSchedule(
    routine: Routine,
    now: Instant,
): RoutineSchedule {
    val durations = routine.data.map { linkDuration(it) }
    val totalDuration = durations.fold(Duration.ZERO) { acc, duration -> acc + duration }
    val recurrence = routine.recurrence
    val effectiveAnchor = if (recurrence.unit == RoutineRecurrenceUnit.NONE) {
        routine.scheduledAt
    } else {
        val interval = max(recurrence.interval, 1)
        val (previousOrEqual, next) = anchorRange(
            base = routine.scheduledAt,
            now = now,
            interval = interval,
            unit = recurrence.unit,
        )
        val currentInstance = buildInstance(previousOrEqual, routine.scheduledAtAnchor, totalDuration)
        if (now < currentInstance.end) previousOrEqual else next
    }
    val effectiveStart = when (routine.scheduledAtAnchor) {
        RoutineScheduleAnchor.START -> effectiveAnchor
        RoutineScheduleAnchor.END -> effectiveAnchor - totalDuration
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
        signature = buildSignature(
            routineTitle = routine.title,
            effectiveStart = effectiveStart,
            end = end,
            tasks = tasks,
            recurrenceUnit = recurrence.unit,
            recurrenceInterval = recurrence.interval.coerceAtLeast(1),
        ),
    )
}

private data class ScheduleInstance(
    val effectiveStart: Instant,
    val end: Instant,
)

private fun buildInstance(
    anchor: Instant,
    scheduleAnchor: RoutineScheduleAnchor,
    totalDuration: Duration,
): ScheduleInstance {
    val effectiveStart = when (scheduleAnchor) {
        RoutineScheduleAnchor.START -> anchor
        RoutineScheduleAnchor.END -> anchor - totalDuration
    }
    return ScheduleInstance(
        effectiveStart = effectiveStart,
        end = effectiveStart + totalDuration,
    )
}

private fun anchorRange(
    base: Instant,
    now: Instant,
    interval: Int,
    unit: RoutineRecurrenceUnit,
): Pair<Instant, Instant> {
    if (unit == RoutineRecurrenceUnit.DAY || unit == RoutineRecurrenceUnit.WEEK) {
        val unitMillis = when (unit) {
            RoutineRecurrenceUnit.DAY -> 24L * 60L * 60L * 1000L
            RoutineRecurrenceUnit.WEEK -> 7L * 24L * 60L * 60L * 1000L
            else -> error("Unsupported unit: $unit")
        }
        val step = unitMillis * interval
        val delta = now.toEpochMilliseconds() - base.toEpochMilliseconds()
        val cycles = Math.floorDiv(delta, step)
        val prev = Instant.fromEpochMilliseconds(base.toEpochMilliseconds() + cycles * step)
        val next = Instant.fromEpochMilliseconds(prev.toEpochMilliseconds() + step)
        return prev to next
    }

    val zoneId = ZoneId.systemDefault()
    val baseZdt = base.toJavaZonedDateTime(zoneId)
    val nowZdt = now.toJavaZonedDateTime(zoneId)
    val prevZdt = when (unit) {
        RoutineRecurrenceUnit.MONTH -> previousByCalendar(
            baseZdt = baseZdt,
            nowZdt = nowZdt,
            interval = interval,
            chronoUnit = ChronoUnit.MONTHS,
        )

        RoutineRecurrenceUnit.YEAR -> previousByCalendar(
            baseZdt = baseZdt,
            nowZdt = nowZdt,
            interval = interval,
            chronoUnit = ChronoUnit.YEARS,
        )

        else -> baseZdt
    }
    val nextZdt = addCalendar(prevZdt, interval, unit)
    return prevZdt.toKotlinInstant() to nextZdt.toKotlinInstant()
}

private fun previousByCalendar(
    baseZdt: ZonedDateTime,
    nowZdt: ZonedDateTime,
    interval: Int,
    chronoUnit: ChronoUnit,
): ZonedDateTime {
    val between = chronoUnit.between(baseZdt, nowZdt)
    var steps = Math.floorDiv(between, interval.toLong())
    var candidate = baseZdt.plus(steps * interval.toLong(), chronoUnit)
    while (candidate > nowZdt) {
        steps -= 1
        candidate = baseZdt.plus(steps * interval.toLong(), chronoUnit)
    }
    while (baseZdt.plus((steps + 1) * interval.toLong(), chronoUnit) <= nowZdt) {
        steps += 1
    }
    return baseZdt.plus(steps * interval.toLong(), chronoUnit)
}

private fun addCalendar(
    value: ZonedDateTime,
    interval: Int,
    unit: RoutineRecurrenceUnit,
): ZonedDateTime = when (unit) {
    RoutineRecurrenceUnit.MONTH -> value.plusMonths(interval.toLong())
    RoutineRecurrenceUnit.YEAR -> value.plusYears(interval.toLong())
    else -> value
}

private fun Instant.toJavaZonedDateTime(zoneId: ZoneId): ZonedDateTime =
    java.time.Instant.ofEpochMilli(toEpochMilliseconds()).atZone(zoneId)

private fun ZonedDateTime.toKotlinInstant(): Instant =
    Instant.fromEpochMilliseconds(toInstant().toEpochMilli())

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
    recurrenceUnit: RoutineRecurrenceUnit,
    recurrenceInterval: Int,
): String = buildString {
    append(routineTitle)
    append('|')
    append(effectiveStart.toEpochMilliseconds())
    append('|')
    append(end.toEpochMilliseconds())
    append('|')
    append(recurrenceUnit.name)
    append(':')
    append(recurrenceInterval)
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
