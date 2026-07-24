package com.xenikii.timecalculator.shared.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
fun AppNavigatorProvider(
    initialBackStack: List<NavKey> = listOf(AppRoute.Home),
    onBackStackCreated: (NavBackStack<NavKey>) -> Unit = {},
    content: @Composable () -> Unit
) {
    val backStack = rememberNavBackStack(*initialBackStack.toTypedArray())
    val navigator = remember(backStack) {
        NavigatorImpl(backStack)
    }
    LaunchedEffect(backStack) {
        onBackStackCreated(backStack)
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