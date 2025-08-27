package com.example.morningcalculator.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.morningcalculator.shared.theme.custom.CustomColorScheme

@Composable
fun MorningCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        else -> LightColorScheme
    }

    val appColorScheme = when {
        else -> LightAppColorScheme
    }

    CompositionLocalProvider(LocalCustomColorScheme provides appColorScheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }

}

val LocalCustomColorScheme = staticCompositionLocalOf<CustomColorScheme> {
    error("No CustomColorScheme provided. Did you forget add it?")
}

//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//        darkTheme -> DarkColorScheme