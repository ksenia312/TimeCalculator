package com.example.morningcalculator.shared.features

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme

@Composable
fun routineCardBackground(
    isOngoing: Boolean,
    isCompleted: Boolean,
): Brush {
    val baseGradient = when {
        isCompleted -> LocalCustomColorScheme.current.label
        isOngoing -> LocalCustomColorScheme.current.accentDark
        else -> MaterialTheme.colorScheme.onBackground
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