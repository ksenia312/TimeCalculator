package com.xenikii.timecalculator.data.schedule.computation

import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.model.RoutineScheduleAnchor
import com.xenikii.timecalculator.domain.model.ScheduledTask
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
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
            daysOfWeek = recurrence.daysOfWeek,
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
            recurrenceDaysOfWeek = recurrence.daysOfWeek,
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
    daysOfWeek: Set<Int>,
): Pair<Instant, Instant> {
    val selectedDays = daysOfWeek.filter { it in 1..7 }.toSortedSet()
    if (unit == RoutineRecurrenceUnit.WEEK && selectedDays.isNotEmpty()) {
        return weeklyDaysAnchorRange(
            base = base,
            now = now,
            interval = interval,
            days = selectedDays,
        )
    }

    if (unit == RoutineRecurrenceUnit.DAY || unit == RoutineRecurrenceUnit.WEEK) {
        val unitMillis = when (unit) {
            RoutineRecurrenceUnit.DAY -> 24L * 60L * 60L * 1000L
            RoutineRecurrenceUnit.WEEK -> 7L * 24L * 60L * 60L * 1000L
            else -> error("Unsupported unit: $unit")
        }
        val step = unitMillis * interval
        val delta = now.toEpochMilliseconds() - base.toEpochMilliseconds()
        // When now is before the repeat start date, the first occurrence is base itself.
        val cycles = Math.floorDiv(delta, step).coerceAtLeast(0)
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
    // When now is before the repeat start date, the first occurrence is base itself.
    if (nowZdt < baseZdt) return baseZdt
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

/**
 * Computes the previous-or-equal and next anchor for a weekly routine that repeats on a specific
 * set of weekdays. [days] holds ISO day-of-week numbers (1 = Monday .. 7 = Sunday). The routine
 * keeps the time-of-day of [base] and fires on every selected weekday of each active week, where an
 * active week repeats every [interval] weeks counting from the week that contains [base].
 * Occurrences never precede the [base] date (the repeat start date).
 */
private fun weeklyDaysAnchorRange(
    base: Instant,
    now: Instant,
    interval: Int,
    days: Set<Int>,
): Pair<Instant, Instant> {
    val zoneId = ZoneId.systemDefault()
    val baseZdt = base.toJavaZonedDateTime(zoneId)
    val nowZdt = now.toJavaZonedDateTime(zoneId)
    val timeOfDay = baseZdt.toLocalTime()
    val baseDate = baseZdt.toLocalDate()
    val baseWeekMonday = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sortedDays = days.toSortedSet()

    fun occurrencesForWeek(weekMonday: LocalDate): List<ZonedDateTime> =
        sortedDays
            .map { weekMonday.plusDays((it - 1).toLong()) }
            .filter { it >= baseDate }
            .map { it.atTime(timeOfDay).atZone(zoneId) }

    // Search enough weeks ahead to guarantee at least one occurrence (a selected day always exists
    // within one active-week span).
    val forwardWeekSpan = interval + 2

    fun firstOccurrence(): ZonedDateTime {
        var weekMonday = baseWeekMonday
        repeat(forwardWeekSpan) {
            occurrencesForWeek(weekMonday).firstOrNull()?.let { return it }
            weekMonday = weekMonday.plusWeeks(interval.toLong())
        }
        return baseDate.atTime(timeOfDay).atZone(zoneId)
    }

    fun occurrenceAfter(anchor: ZonedDateTime): ZonedDateTime {
        var weekMonday = anchor.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        repeat(forwardWeekSpan) {
            occurrencesForWeek(weekMonday).firstOrNull { it > anchor }?.let { return it }
            weekMonday = weekMonday.plusWeeks(interval.toLong())
        }
        return anchor.plusWeeks(interval.toLong())
    }

    val firstOccurrence = firstOccurrence()
    if (nowZdt < firstOccurrence) {
        return firstOccurrence.toKotlinInstant() to occurrenceAfter(firstOccurrence).toKotlinInstant()
    }

    // Align to the active week on or before now, then walk back to find the latest anchor <= now.
    val nowWeekMonday = nowZdt.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weeksFromBase = ChronoUnit.WEEKS.between(baseWeekMonday, nowWeekMonday).coerceAtLeast(0)
    val alignedWeeks = weeksFromBase - (weeksFromBase % interval.toLong())
    var weekMonday = baseWeekMonday.plusWeeks(alignedWeeks)
    var previousOrEqual: ZonedDateTime = firstOccurrence
    while (weekMonday >= baseWeekMonday) {
        val occ = occurrencesForWeek(weekMonday).lastOrNull { it <= nowZdt }
        if (occ != null) {
            previousOrEqual = occ
            break
        }
        weekMonday = weekMonday.minusWeeks(interval.toLong())
    }

    return previousOrEqual.toKotlinInstant() to occurrenceAfter(previousOrEqual).toKotlinInstant()
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
    recurrenceDaysOfWeek: Set<Int>,
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
    append(':')
    append(recurrenceDaysOfWeek.toSortedSet().joinToString(separator = ","))
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
