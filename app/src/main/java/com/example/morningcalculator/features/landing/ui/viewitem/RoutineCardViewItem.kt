package com.example.morningcalculator.features.landing.ui.viewitem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.shared.extensions.currentDuration
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
    val currentDuration: String
) {
    companion object Companion {
        @Composable
        fun create(routine: Routine): RoutineCardViewItem {
            rememberNow(tickMillis = 1000L)

            val isOngoing = routine.isOngoing()
            val isCompleted = routine.isCompleted()
            val startLabel = when {
                isOngoing || isCompleted -> "Started at"
                else -> "Will start"
            }

            val endLabel = when {
                isOngoing -> "Ends at"
                isCompleted -> "Completed at"
                else -> "Will end"
            }

            val startText = routine.whenToStart().stringDateTime()
            val endText = routine.endAt().stringDateTime()
            val currentDuration = routine.currentDuration()

            return RoutineCardViewItem(
                isOngoing = isOngoing,
                isCompleted = isCompleted,
                startLabel = startLabel,
                endLabel = endLabel,
                startText = startText,
                endText = endText,
                currentDuration = currentDuration.stringValue(),
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