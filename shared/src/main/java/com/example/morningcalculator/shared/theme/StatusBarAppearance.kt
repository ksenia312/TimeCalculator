package com.example.morningcalculator.shared.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Adjusts the status bar icon color for the current screen.
 *
 * The app always renders light/white surfaces (see [MorningCalculatorTheme], which forces the light
 * color scheme regardless of the system dark mode), so ordinary screens always need dark status bar
 * icons - independently of the device's system theme. Only a bright/accent top bar needs light
 * icons.
 *
 * @param hasBrightTopBar whether the current screen shows a bright/accent top bar that reaches
 * under the status bar. When true the icons are forced to light (white); otherwise they are dark
 * (black) so they stay visible on the light bar on every device.
 */
@Composable
fun SetStatusBarForBrightTopBar(hasBrightTopBar: Boolean) {
    val view = LocalView.current
    LaunchedEffect(view, hasBrightTopBar) {
        val window = view.context.findActivity()?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = !hasBrightTopBar
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
