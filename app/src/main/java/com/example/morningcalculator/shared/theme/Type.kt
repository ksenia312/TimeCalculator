package com.example.morningcalculator.shared.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import com.example.morningcalculator.shared.theme.font.OnestFamily

private val defaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.withOnest(),
    displayMedium = defaultTypography.displayMedium.withOnest(),
    displaySmall = defaultTypography.displaySmall.withOnest(),

    headlineLarge = defaultTypography.headlineLarge.withOnest(),
    headlineMedium = defaultTypography.headlineMedium.withOnest(),
    headlineSmall = defaultTypography.headlineSmall.withOnest(),

    titleLarge = defaultTypography.titleLarge.withOnest(),
    titleMedium = defaultTypography.titleMedium.withOnest(),
    titleSmall = defaultTypography.titleSmall.withOnest(),

    bodyLarge = defaultTypography.bodyLarge.withOnest(),
    bodyMedium = defaultTypography.bodyMedium.withOnest(),
    bodySmall = defaultTypography.bodySmall.withOnest(),

    labelLarge = defaultTypography.labelLarge.withOnest(),
    labelMedium = defaultTypography.labelMedium.withOnest(),
    labelSmall = defaultTypography.labelSmall.withOnest()
)

private fun TextStyle.withOnest() = copy(fontFamily = OnestFamily)