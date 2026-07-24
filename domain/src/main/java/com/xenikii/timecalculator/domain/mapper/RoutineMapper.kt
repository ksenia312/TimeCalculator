package com.xenikii.timecalculator.domain.mapper

import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineRequest

fun Routine.copyWithRequest(request: RoutineRequest): Routine {
    return this.copy(
        title = request.title,
        scheduledAt = request.scheduledAt,
        scheduledAtAnchor = request.scheduledAtAnchor,
        color = request.color
    )
}