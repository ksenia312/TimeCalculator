package com.example.morningcalculator.shared.theme

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.morningcalculator.shared.navigator.LocalNavHostController

@Composable
fun PreviewTheme(
    colorOverride: Color? = null,
    content: @Composable () -> Unit
) {
    val appColorScheme = when {
        else -> LightAppColorScheme
    }
    val navController = rememberNavController()

    CompositionLocalProvider(
        LocalCustomColorScheme provides appColorScheme,
        LocalNavHostController provides navController,
    ) {
        // using Surface because it is a background and text might use the onBackground color
        Surface(
            color = colorOverride ?: androidx.compose.material3.MaterialTheme.colorScheme.background,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        ) {
            content()
        }
    }

}