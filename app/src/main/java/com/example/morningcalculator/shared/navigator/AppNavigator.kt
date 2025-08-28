package com.example.morningcalculator.shared.navigator

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.morningcalculator.features.home.ui.HomeScreen
import com.example.morningcalculator.features.routine.ui.RoutineScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Routine : Screen("routine")
}

@Composable
fun AppNavigator() {
    AppNavigatorProvider {
        val navController = LocalNavHostController.current
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(200)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(200)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(200)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(200)
                )
            }
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Routine.route) {
                val result =
                    navController.currentBackStackEntry?.savedStateHandle?.get<String>("routineId")

                val id = remember { result ?: "" }
                RoutineScreen(id = id)
            }
        }
    }
}