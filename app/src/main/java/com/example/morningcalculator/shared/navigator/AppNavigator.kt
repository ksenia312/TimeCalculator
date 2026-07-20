package com.example.morningcalculator.shared.navigator

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.example.morningcalculator.features.routineeditor.ui.CreateRoutineScreen
import com.example.morningcalculator.features.routineeditor.ui.EditRoutineScreen
import com.example.morningcalculator.features.home.ui.HomeScreen
import com.example.morningcalculator.features.routine.ui.RoutineScreen
import com.example.morningcalculator.features.taskeditor.ui.CreateTaskScreen
import com.example.morningcalculator.features.taskeditor.ui.EditTaskScreen

@Composable
fun AppNavigator() {
    AppNavigatorProvider {
        val backStack = LocalNavigationBackStack.current

        NavDisplay(
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            backStack = backStack,
            transitionSpec = transitionSpec(),
            popTransitionSpec = popTransitionSpec(),
            predictivePopTransitionSpec = predictivePopTransitionSpec(),
            entryProvider = { key ->
                when (key) {
                    AppRoute.Home -> {
                        NavEntry(key = key) { HomeScreen() }
                    }

                    is AppRoute.Routine -> {
                        NavEntry(key = key) {
                            RoutineScreen(id = key.routineId)
                        }
                    }

                    is AppRoute.CreateTask -> {
                        NavEntry(key = key) {
                            CreateTaskScreen(routineId = key.routineId)
                        }
                    }

                    is AppRoute.EditTask -> {
                        NavEntry(key = key) {
                            EditTaskScreen(arguments = key.arguments)
                        }
                    }

                    AppRoute.CreateRoutine -> {
                        NavEntry(key = key) {
                            CreateRoutineScreen()
                        }
                    }

                    is AppRoute.EditRoutine -> {
                        NavEntry(key = key) {
                            EditRoutineScreen(
                                routineId = key.routineId,
                                fromRoutineScreen = key.fromRoutineScreen,
                            )
                        }
                    }

                    else -> error("Unknown NavKey: $key")
                }
            }
        )
    }
}

private fun transitionSpec(): AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    {
        slideInHorizontally(
            animationSpec = tween(200),
            initialOffsetX = { it },
        ) + fadeIn() togetherWith slideOutHorizontally(
            animationSpec = tween(200),
            targetOffsetX = { -it },
        ) + fadeOut()
    }

private fun popTransitionSpec(): AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    {
        slideInHorizontally(
            animationSpec = tween(200),
            initialOffsetX = { -it },
        ) + fadeIn() togetherWith slideOutHorizontally(
            animationSpec = tween(200),
            targetOffsetX = { it },
        ) + fadeOut()
    }

private fun predictivePopTransitionSpec(): AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform =
    {
        slideInHorizontally(
            animationSpec = tween(200),
            initialOffsetX = { -it },
        ) + fadeIn() togetherWith slideOutHorizontally(
            animationSpec = tween(200),
            targetOffsetX = { it },
        ) + fadeOut()
    }
