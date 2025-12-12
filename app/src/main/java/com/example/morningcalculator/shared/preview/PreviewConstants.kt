package com.example.morningcalculator.shared.preview

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.minutes

object PreviewConstants {
    val routines = List(10) {
        Routine.Links(
            id = it.toString(),
            title = "Morning Routine",
            color = "0xFFE57373",
            time = LocalTime(7, 0),
            modifiedAt = System.currentTimeMillis(),
            links = listOf()
        )
    }

    val routinesFull = List(10) {
        Routine.Full(
            id = it.toString(),
            title = "Morning Routine",
            color = "0xFFE57373",
            time = LocalTime(7, 0),
            modifiedAt = System.currentTimeMillis(),
            data = listOf()
        )
    }

    val tasks = List(10) {
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
}