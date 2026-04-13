package com.example.morningcalculator.features.landing.ui.viewitem

import androidx.compose.runtime.Composable
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.shared.extensions.endAt
import com.example.morningcalculator.shared.extensions.isCompleted
import com.example.morningcalculator.shared.extensions.isOngoing
import com.example.morningcalculator.shared.extensions.stringDateTime
import com.example.morningcalculator.shared.extensions.stringValue
import com.example.morningcalculator.shared.extensions.whenToStart
import com.example.morningcalculator.shared.extensions.currentDuration

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