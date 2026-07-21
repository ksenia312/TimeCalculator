package com.example.morningcalculator.shared.viewitem

import com.example.morningcalculator.R
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineSchedule
import com.example.morningcalculator.domain.model.RoutineSchedulePhase
import kotlin.time.Instant

fun Routine.toViewItem(
    schedule: RoutineSchedule,
    now: Instant,
): RoutineCardViewItem {
    val isOngoing = schedule.phaseAt(now) == RoutineSchedulePhase.ACTIVE
    val isCompleted = schedule.phaseAt(now) == RoutineSchedulePhase.FINISHED
    return RoutineCardViewItem(
        isOngoing = isOngoing,
        isCompleted = isCompleted,
        startLabelRes = if (isOngoing || isCompleted) {
            R.string.routine_card_started_at
        } else {
            R.string.routine_card_will_start
        },
        endLabelRes = when {
            isOngoing -> R.string.routine_card_ends_at
            isCompleted -> R.string.routine_card_completed_at
            else -> R.string.routine_card_will_end
        },
        startInstant = schedule.effectiveStart,
        endInstant = schedule.end,
        title = this.title,
        willStartIn = schedule.effectiveStart - now,
    )
}
