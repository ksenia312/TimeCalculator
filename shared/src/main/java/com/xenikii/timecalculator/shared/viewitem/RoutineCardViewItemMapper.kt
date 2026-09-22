package com.xenikii.timecalculator.shared.viewitem

import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.domain.model.RoutineSchedulePhase
import kotlin.time.Instant

fun Routine.toViewItem(
    schedule: RoutineSchedule,
    now: Instant,
): RoutineCardViewItem {
    val phase = schedule.phaseAt(now)
    val isOngoing = phase == RoutineSchedulePhase.ACTIVE
    val isCompleted = phase == RoutineSchedulePhase.FINISHED
    return RoutineCardViewItem(
        status = when {
            isPaused -> RoutineCardStatus.PAUSED
            isOngoing -> RoutineCardStatus.ONGOING
            isCompleted -> RoutineCardStatus.COMPLETED
            else -> RoutineCardStatus.PLANNED
        },
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
        recurrence = recurrence,
    )
}
