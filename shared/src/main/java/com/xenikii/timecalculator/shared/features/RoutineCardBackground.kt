package com.xenikii.timecalculator.shared.features

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme
import com.xenikii.timecalculator.shared.viewitem.RoutineCardStatus

@Composable
fun routineCardBackground(status: RoutineCardStatus): Brush {
    val baseGradient = when (status) {
        RoutineCardStatus.COMPLETED -> LocalCustomColorScheme.current.label
        RoutineCardStatus.ONGOING -> LocalCustomColorScheme.current.accentDark
        RoutineCardStatus.PLANNED, RoutineCardStatus.PAUSED -> MaterialTheme.colorScheme.onBackground
    }

    return Brush.linearGradient(
        listOf(
            baseGradient,
            baseGradient.copy(alpha = 0.85f),
            baseGradient.copy(alpha = 0.72f),
        )
    )
}

@Composable
fun routineStatusDotColor(isOngoing: Boolean): Color {
    return if (isOngoing) {
        LocalCustomColorScheme.current.success
    } else {
        LocalCustomColorScheme.current.unselected
    }
}