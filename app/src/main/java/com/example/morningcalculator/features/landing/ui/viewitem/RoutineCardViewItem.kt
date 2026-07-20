package com.example.morningcalculator.features.landing.ui.viewitem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.shared.extensions.willStartIn
import com.example.morningcalculator.shared.extensions.endAt
import com.example.morningcalculator.shared.extensions.isCompleted
import com.example.morningcalculator.shared.extensions.isOngoing
import com.example.morningcalculator.shared.extensions.stringDateTime
import com.example.morningcalculator.shared.extensions.stringValue
import com.example.morningcalculator.shared.extensions.whenToStart
import kotlinx.coroutines.delay
import kotlin.time.Instant

data class RoutineCardViewItem(
    val isOngoing: Boolean,
    val isCompleted: Boolean,
    val startLabel: String,
    val endLabel: String,
    val startText: String,
    val endText: String,
    val title: String,
    val willStartIn: String
) {
    companion object Companion {
        @Composable
        fun create(routine: Routine): RoutineCardViewItem {
            rememberNow(tickMillis = 1000L)
            val context = LocalContext.current

            val isOngoing = routine.isOngoing()
            val isCompleted = routine.isCompleted()
            val startLabel = when {
                isOngoing || isCompleted -> stringResource(R.string.routine_card_started_at)
                else -> stringResource(R.string.routine_card_will_start)
            }

            val endLabel = when {
                isOngoing -> stringResource(R.string.routine_card_ends_at)
                isCompleted -> stringResource(R.string.routine_card_completed_at)
                else -> stringResource(R.string.routine_card_will_end)
            }

            val startText = routine.whenToStart().stringDateTime(context = context)
            val endText = routine.endAt().stringDateTime(context = context)
            val willStartIn = routine.willStartIn()

            return RoutineCardViewItem(
                isOngoing = isOngoing,
                isCompleted = isCompleted,
                startLabel = startLabel,
                endLabel = endLabel,
                startText = startText,
                endText = endText,
                willStartIn = willStartIn.stringValue(context),
                title = routine.title
            )
        }
    }
}

@Composable
fun rememberNow(
    tickMillis: Long = 1000L,
): Instant {
    val isPreview = LocalInspectionMode.current

    val now by produceState(
        initialValue = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
        key1 = tickMillis,
        key2 = isPreview,
    ) {
        if (isPreview) return@produceState

        while (true) {
            value = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            delay(tickMillis)
        }
    }

    return now
}