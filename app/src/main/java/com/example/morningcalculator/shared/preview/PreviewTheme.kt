package com.example.morningcalculator.shared.preview

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.morningcalculator.shared.navigator.AppRoute
import com.example.morningcalculator.shared.navigator.LocalNavigationBackStack
import com.example.morningcalculator.shared.navigator.LocalNavigator
import com.example.morningcalculator.shared.navigator.NavigatorImpl
import com.example.morningcalculator.shared.theme.LightAppColorScheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import com.example.morningcalculator.shared.theme.MorningCalculatorTheme

@Composable
fun PreviewTheme(
    colorOverride: Color? = null,
    content: @Composable () -> Unit
) {

    val appColorScheme = when {
        else -> LightAppColorScheme
    }
    val backStack = rememberNavBackStack(AppRoute.Home)
    val navigator = remember(backStack) { NavigatorImpl(backStack) }

    CompositionLocalProvider(
        LocalCustomColorScheme provides appColorScheme,
        LocalNavigator provides navigator,
        LocalNavigationBackStack provides backStack,
    ) {
        MorningCalculatorTheme {
            // using Surface because it is a background and text might use the onBackground color
            Surface(
                color = colorOverride ?: MaterialTheme.colorScheme.background,
                modifier = Modifier.wrapContentSize(Alignment.Center)
            ) {
                content()
            }
        }

    }
}