package com.example.morningcalculator.shared.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
fun AppNavigatorProvider(
    startAppRoute: AppRoute = AppRoute.Home,
    content: @Composable () -> Unit
) {
    val backStack = rememberNavBackStack(startAppRoute)
    val navigator = remember(backStack) {
        NavigatorImpl(backStack)
    }
    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalNavigationBackStack provides backStack,
    ) {
        content()
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No Navigator provided")
}

val LocalNavigationBackStack = staticCompositionLocalOf<NavBackStack<NavKey>> {
    error("No NavigationBackStack provided")
}