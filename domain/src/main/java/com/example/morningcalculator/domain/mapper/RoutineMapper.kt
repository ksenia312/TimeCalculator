package com.example.morningcalculator.domain.mapper

import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineRequest

fun Routine.copyWithRequest(request: RoutineRequest): Routine {
    return this.copy(
        title = request.title,
        scheduledAt = request.scheduledAt,
        scheduledAtAnchor = request.scheduledAtAnchor,
        color = request.color
    )
}