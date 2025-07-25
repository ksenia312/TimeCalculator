package com.example.morningcalculator.shared.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigatorProvider(
    content: @Composable () -> Unit
) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavHostController provides navController) {
        content()
    }
}

val LocalNavHostController = staticCompositionLocalOf<NavHostController> {
    error("No NavHostController provided")
}