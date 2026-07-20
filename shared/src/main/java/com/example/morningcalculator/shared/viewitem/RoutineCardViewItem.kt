package com.example.morningcalculator.shared.viewitem

import androidx.annotation.StringRes
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.shared.extensions.endAtInstant
import com.example.morningcalculator.shared.extensions.startAtInstant
import com.example.morningcalculator.shared.extensions.willStartIn
import kotlin.time.Duration
import kotlin.time.Instant

data class RoutineCardViewItem(
    val isOngoing: Boolean,
    val isCompleted: Boolean,
    @param:StringRes val startLabelRes: Int,
    @param:StringRes val endLabelRes: Int,
    val startInstant: Instant,
    val endInstant: Instant,
    val title: String,
    val willStartIn: Duration,
)

fun Routine.toRoutineCardViewItem(
    now: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
): RoutineCardViewItem {
    val startInstant = startAtInstant()
    val endInstant = endAtInstant()
    val isOngoing = now in startInstant..endInstant
    val isCompleted = now > endInstant

    val startLabelRes = when {
        isOngoing || isCompleted -> R.string.routine_card_started_at
        else -> R.string.routine_card_will_start
    }
    val endLabelRes = when {
        isOngoing -> R.string.routine_card_ends_at
        isCompleted -> R.string.routine_card_completed_at
        else -> R.string.routine_card_will_end
    }

    return RoutineCardViewItem(
        isOngoing = isOngoing,
        isCompleted = isCompleted,
        startLabelRes = startLabelRes,
        endLabelRes = endLabelRes,
        startInstant = startInstant,
        endInstant = endInstant,
        willStartIn = willStartIn(),
        title = title,
    )
}
