package com.example.morningcalculator.core.mapper

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest

fun Routine.copyWithRequest(request: RoutineRequest): Routine {
    return this.copy(
        title = request.title,
        scheduledAt = request.scheduledAt,
        scheduledAtAnchor = request.scheduledAtAnchor,
        color = request.color
    )
}