package com.example.morningcalculator.data.mapper

import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineLink
import com.example.morningcalculator.domain.model.RoutineScheduleAnchor
import com.example.morningcalculator.domain.model.SubData
import com.example.morningcalculator.domain.model.Task
import com.example.morningcalculator.data.db.RoutinePopulated
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

    return Routine(
        id = routine.id,
        title = routine.title,
        color = routine.color,
        scheduledAt = Instant.fromEpochMilliseconds(routine.scheduledAtMillis),
        scheduledAtAnchor = anchor,
        modifiedAt = routine.modifiedAt,
        data = fullLinks
    )
}