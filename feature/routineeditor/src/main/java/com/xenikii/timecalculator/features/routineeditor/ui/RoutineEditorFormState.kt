package com.xenikii.timecalculator.features.routineeditor.ui

import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineRecurrence
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.domain.model.RoutineScheduleAnchor
import com.xenikii.timecalculator.shared.extensions.withZeroSeconds
import kotlinx.datetime.LocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Instant

data class RoutineEditorFormState(
    val isVisible: Boolean = false,
    val routineId: String? = null,
    val title: String = "",
    val anchor: RoutineScheduleAnchor = RoutineScheduleAnchor.END,
    val recurrenceUnit: RoutineRecurrenceUnit = RoutineRecurrenceUnit.NONE,
    val recurrenceInterval: Int = 1,
    val recurrenceDaysOfWeek: Set<Int> = emptySet(),
    val date: LocalDate = LocalDate.now(ZoneId.systemDefault()).plusDays(1),
    val time: LocalTime = LocalTime(7, 0),
)

/** ISO day-of-week numbers (1 = Monday .. 7 = Sunday) accepted by the weekly recurrence editor. */
private val ISO_DAYS_OF_WEEK = 1..7

fun Routine.toRoutineEditorFormState(zoneId: ZoneId = ZoneId.systemDefault()): RoutineEditorFormState {
    val initialDateTime = java.time.Instant
        .ofEpochMilli(scheduledAt.toEpochMilliseconds())
        .atZone(zoneId)
        .toLocalDateTime()

    val date = initialDateTime.toLocalDate()
    val recurrenceDaysOfWeek = recurrence.daysOfWeek
        .filter { it in ISO_DAYS_OF_WEEK }
        .toSet()
        .ifEmpty {
            if (recurrence.unit == RoutineRecurrenceUnit.WEEK) setOf(date.dayOfWeek.value)
            else emptySet()
        }

    return RoutineEditorFormState(
        isVisible = true,
        routineId = id,
        title = title,
        anchor = scheduledAtAnchor,
        recurrenceUnit = recurrence.unit,
        recurrenceInterval = recurrence.interval,
        recurrenceDaysOfWeek = recurrenceDaysOfWeek,
        date = date,
        time = LocalTime(initialDateTime.hour, initialDateTime.minute),
    )
}

/**
 * The weekdays the routine repeats on, but only while weekly recurrence is active. Any other
 * recurrence unit yields an empty set so stale day selections can't leak into non-weekly
 * recurrences. Values outside the ISO range (1 = Monday .. 7 = Sunday) are dropped.
 */
fun RoutineEditorFormState.effectiveRecurrenceDaysOfWeek(): Set<Int> =
    if (recurrenceUnit == RoutineRecurrenceUnit.WEEK) {
        recurrenceDaysOfWeek
            .filter { it in ISO_DAYS_OF_WEEK }
            .toSet()
    } else {
        emptySet()
    }

/**
 * Applies a [recurrenceUnit] change. When weekly recurrence is switched on and no valid day is
 * currently selected, the selected [date]'s weekday is seeded as a default, mirroring the one-time
 * seeding in [toRoutineEditorFormState]. Existing selections are preserved as-is.
 */
fun RoutineEditorFormState.withRecurrenceUnit(unit: RoutineRecurrenceUnit): RoutineEditorFormState {
    val activatingWeekly = unit == RoutineRecurrenceUnit.WEEK && recurrenceUnit != RoutineRecurrenceUnit.WEEK
    val seededDays = if (activatingWeekly && recurrenceDaysOfWeek.none { it in ISO_DAYS_OF_WEEK }) {
        setOf(date.dayOfWeek.value)
    } else {
        recurrenceDaysOfWeek
    }
    return copy(recurrenceUnit = unit, recurrenceDaysOfWeek = seededDays)
}

/**
 * Applies a [date] change. While weekly recurrence is active the day selection follows the picked
 * date, collapsing to that date's weekday; for every other recurrence unit the current selection is
 * left untouched.
 */
fun RoutineEditorFormState.withDate(date: LocalDate): RoutineEditorFormState {
    val seededDays = if (recurrenceUnit == RoutineRecurrenceUnit.WEEK) {
        setOf(date.dayOfWeek.value)
    } else {
        recurrenceDaysOfWeek
    }
    return copy(date = date, recurrenceDaysOfWeek = seededDays)
}

fun RoutineEditorFormState.toRoutineRecurrence(): RoutineRecurrence = RoutineRecurrence(
    interval = recurrenceInterval.coerceAtLeast(1),
    unit = recurrenceUnit,
    daysOfWeek = effectiveRecurrenceDaysOfWeek(),
)

fun RoutineEditorFormState.toScheduledAtInstant(
    zoneId: ZoneId = ZoneId.systemDefault(),
): Instant {
    val scheduledAtMillis = LocalDateTime
        .of(date, java.time.LocalTime.of(time.hour, time.minute))
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()

    return Instant
        .fromEpochMilliseconds(scheduledAtMillis)
        .withZeroSeconds()
}

fun Routine.applyRoutineEditorFormState(
    routineEditorFormState: RoutineEditorFormState,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Routine = copy(
    title = routineEditorFormState.title,
    scheduledAt = routineEditorFormState.toScheduledAtInstant(zoneId),
    scheduledAtAnchor = routineEditorFormState.anchor,
    recurrence = routineEditorFormState.toRoutineRecurrence(),
)
