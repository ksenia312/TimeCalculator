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

fun Routine.toRoutineEditorFormState(zoneId: ZoneId = ZoneId.systemDefault()): RoutineEditorFormState {
    val initialDateTime = java.time.Instant
        .ofEpochMilli(scheduledAt.toEpochMilliseconds())
        .atZone(zoneId)
        .toLocalDateTime()

    return RoutineEditorFormState(
        isVisible = true,
        routineId = id,
        title = title,
        anchor = scheduledAtAnchor,
        recurrenceUnit = recurrence.unit,
        recurrenceInterval = recurrence.interval,
        recurrenceDaysOfWeek = recurrence.daysOfWeek,
        date = initialDateTime.toLocalDate(),
        time = LocalTime(initialDateTime.hour, initialDateTime.minute, 0, 0),
    )
}

/**
 * The weekdays the routine repeats on, but only when weekly recurrence is active. Prevents stale
 * day selections from leaking into non-weekly recurrences. When no day has been picked yet, the
 * weekday of the selected start [date] is used as the default, so "repeat weekly from Sep 1
 * (Tuesday)" implies every Tuesday until the user changes the selection.
 */
fun RoutineEditorFormState.effectiveRecurrenceDaysOfWeek(): Set<Int> =
    if (recurrenceUnit == RoutineRecurrenceUnit.WEEK) {
        recurrenceDaysOfWeek
            .filter { it in 1..7 }
            .toSet()
            .ifEmpty { setOf(date.dayOfWeek.value) }
    } else {
        emptySet()
    }

fun RoutineEditorFormState.toScheduledAtInstant(
    zoneId: ZoneId = ZoneId.systemDefault(),
): Instant {
    val scheduledAtMillis = LocalDateTime
        .of(date, java.time.LocalTime.of(time.hour, time.minute, 0, 0))
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
    recurrence = RoutineRecurrence(
        interval = routineEditorFormState.recurrenceInterval.coerceAtLeast(1),
        unit = routineEditorFormState.recurrenceUnit,
        daysOfWeek = routineEditorFormState.effectiveRecurrenceDaysOfWeek(),
    ),
)
