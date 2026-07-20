package com.example.morningcalculator.features.home.ui.components

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineScheduleAnchor
import com.example.morningcalculator.shared.extensions.withZeroSeconds
import kotlinx.datetime.LocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Instant

data class RoutineDialogViewState(
    val isVisible: Boolean = false,
    val routineId: String? = null,
    val title: String = "",
    val anchor: RoutineScheduleAnchor = RoutineScheduleAnchor.END,
    val date: LocalDate = LocalDate.now(ZoneId.systemDefault()).plusDays(1),
    val time: LocalTime = LocalTime(7, 0),
)

fun Routine.toRoutineDialogViewState(zoneId: ZoneId = ZoneId.systemDefault()): RoutineDialogViewState {
    val initialDateTime = java.time.Instant
        .ofEpochMilli(scheduledAt.toEpochMilliseconds())
        .atZone(zoneId)
        .toLocalDateTime()

    return RoutineDialogViewState(
        isVisible = true,
        routineId = id,
        title = title,
        anchor = scheduledAtAnchor,
        date = initialDateTime.toLocalDate(),
        time = LocalTime(initialDateTime.hour, initialDateTime.minute, 0, 0),
    )
}

fun RoutineDialogViewState.toScheduledAtInstant(
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

fun Routine.applyRoutineDialogViewState(
    routineDialogViewState: RoutineDialogViewState,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Routine = copy(
    title = routineDialogViewState.title,
    scheduledAt = routineDialogViewState.toScheduledAtInstant(zoneId),
    scheduledAtAnchor = routineDialogViewState.anchor,
)
