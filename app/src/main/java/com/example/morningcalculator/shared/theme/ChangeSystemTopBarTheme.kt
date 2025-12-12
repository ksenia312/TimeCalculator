package com.example.morningcalculator.shared.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

@Composable
fun ChangeSystemTopBarTheme(
    color: Color
) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    LaunchedEffect(color) {
        val isForegroundDark = color.luminance() < 0.80f
        val barColor = color.toArgb()
        activity.enableEdgeToEdge(
            statusBarStyle = if (isForegroundDark) SystemBarStyle.light(
                barColor, barColor
            ) else SystemBarStyle.dark(barColor),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb()
            )
        )
    }
}
