package com.example.morningcalculator.features.routineeditor.ui

import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineScheduleAnchor
import com.example.morningcalculator.shared.extensions.withZeroSeconds
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
)
