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
        date = initialDateTime.toLocalDate(),
        time = LocalTime(initialDateTime.hour, initialDateTime.minute, 0, 0),
    )
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
    ),
)
