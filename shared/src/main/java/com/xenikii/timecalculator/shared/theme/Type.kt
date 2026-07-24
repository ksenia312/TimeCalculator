package com.xenikii.timecalculator.shared.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import com.xenikii.timecalculator.shared.theme.font.OnestFamily

private fun TextStyle.withOnest() = copy(fontFamily = OnestFamily)

val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.withOnest(),
        displayMedium = displayMedium.withOnest(),
        displaySmall = displaySmall.withOnest(),
        headlineLarge = headlineLarge.withOnest(),
        headlineMedium = headlineMedium.withOnest(),
        headlineSmall = headlineSmall.withOnest(),
        titleLarge = titleLarge.withOnest(),
        titleMedium = titleMedium.withOnest(),
        titleSmall = titleSmall.withOnest(),
        bodyLarge = bodyLarge.withOnest(),
        bodyMedium = bodyMedium.withOnest(),
        bodySmall = bodySmall.withOnest(),
        labelLarge = labelLarge.withOnest(),
        labelMedium = labelMedium.withOnest(),
        labelSmall = labelSmall.withOnest()
    )
}