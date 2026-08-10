package com.xenikii.timecalculator.data.mapper

import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.RoutineRecurrence
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.domain.model.RoutineScheduleAnchor
import com.xenikii.timecalculator.domain.model.SubData
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.data.db.RoutinePopulated
import kotlin.time.Instant

fun RoutinePopulated.toDomain(): Routine {
    val sortedItems = items.sortedBy { it.item.orderIndex }

    val fullLinks = sortedItems.map { itemPopulated ->
        val taskWithData = itemPopulated.taskWithData
        val taskEntity = taskWithData.task
        val allSubDataEntities = taskWithData.subDataList

        RoutineLink(
            id = itemPopulated.item.id,
            task = Task(
                id = taskEntity.id,
                title = taskEntity.title,
                description = taskEntity.description,
                data = allSubDataEntities.map { SubData(it.id, it.duration) },
                modifiedAt = taskEntity.modifiedAt
            ),
            subData = itemPopulated.subData?.let { SubData(it.id, it.duration) }
        )
    }

    val anchor = runCatching {
        RoutineScheduleAnchor.valueOf(routine.scheduledAtAnchor)
    }.getOrDefault(RoutineScheduleAnchor.START)
    val recurrenceUnit = runCatching {
        RoutineRecurrenceUnit.valueOf(routine.recurrenceUnit)
    }.getOrDefault(RoutineRecurrenceUnit.NONE)

    return Routine(
        id = routine.id,
        title = routine.title,
        color = routine.color,
        scheduledAt = Instant.fromEpochMilliseconds(routine.scheduledAtMillis),
        scheduledAtAnchor = anchor,
        recurrence = RoutineRecurrence(
            interval = routine.recurrenceInterval.coerceAtLeast(1),
            unit = recurrenceUnit,
        ),
        modifiedAt = routine.modifiedAt,
        data = fullLinks
    )
}