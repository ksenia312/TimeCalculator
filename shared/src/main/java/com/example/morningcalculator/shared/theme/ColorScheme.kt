package com.example.morningcalculator.shared.theme

import androidx.compose.material3.lightColorScheme
import com.example.morningcalculator.shared.theme.custom.CustomColorScheme

//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)

val LightColorScheme = lightColorScheme(
    background = ExtraLightGray,
    onBackground = Black,
    primary = DarkGray,
    secondary = Gray,
    surface = White,
    onSurface = Black,
    outline = LightGray,
    error = Red,
    onError = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    tertiary = DarkGray,
    primaryContainer = LightGray,
    onPrimaryContainer = Black,
    inversePrimary = LightGray,
    secondaryContainer = ExtraLightGray,
    onSecondaryContainer = Black,
    tertiaryContainer = ExtraLightGray,
    onTertiaryContainer = Black,
    errorContainer = LightGray,
    onErrorContainer = Black,
    surfaceVariant = LightGray,
    onSurfaceVariant = LighterGray,
    inverseSurface = DarkGray,
    inverseOnSurface = White,
    outlineVariant = ExtraLightGray,
    surfaceTint = Transparent,
    scrim = Black,
    surfaceContainerLow = White,
    surfaceBright = White,
    surfaceContainerHigh = White,
    surfaceContainerHighest = ExtraLightGray,
    surfaceContainerLowest = DarkGray,
    surfaceDim = White,
    surfaceContainer = White
)

val LightAppColorScheme = CustomColorScheme(
    accent = Purple,
    accentLight = LightPurple,
    accentDark = DarkPurple,
    label = PalePurple,
    unselected = LighterGray2,
    placeholder = LighterGray2,
    success = Green3
)