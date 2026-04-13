package com.example.morningcalculator.shared.preview

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

object PreviewConstants {
    val tasks: List<Task>
        get() = List(10) {
            Task(
                id = it.toString(),
                title = "Task #$it",
                description = "This is a description for task #$it",
                data = listOf(
                    SubData(
                        duration = 23.minutes
                    )
                )
            )
        }

    val routinesFull = List(10) {
        Routine(
            id = it.toString(),
            title = "Morning Routine",
            color = "0xFFE57373",
            scheduledAt = Instant.fromEpochMilliseconds(
                System.currentTimeMillis() + it * 60L * 60L * 1000L
            ),
            modifiedAt = System.currentTimeMillis(),
            data = tasks.map { task ->
                RoutineLink(
                    id = task.id,
                    task = task,
                    subData = SubData(
                        duration = 23.minutes
                    )
                )
            }
        )
    }
}