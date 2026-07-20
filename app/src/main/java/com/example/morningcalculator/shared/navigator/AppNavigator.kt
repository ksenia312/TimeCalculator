package com.example.morningcalculator.shared.navigator

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.example.morningcalculator.features.home.ui.HomeScreen
import com.example.morningcalculator.features.routine.ui.RoutineScreen
import com.example.morningcalculator.features.routineeditor.ui.CreateRoutineScreen
import com.example.morningcalculator.features.routineeditor.ui.EditRoutineScreen
import com.example.morningcalculator.features.taskeditor.ui.CreateTaskScreen
import com.example.morningcalculator.features.taskeditor.ui.EditTaskScreen
import com.example.morningcalculator.shared.animation.LocalCardAnimatedContentScope
import com.example.morningcalculator.shared.animation.LocalSharedTransitionScope
import com.example.morningcalculator.shared.theme.SetStatusBarForBrightTopBar

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigator() {
    AppNavigatorProvider {
        val backStack = LocalNavigationBackStack.current
        val currentRoute = backStack.lastOrNull() as? AppRoute
        SetStatusBarForBrightTopBar(hasBrightTopBar = currentRoute?.hasBrightTopBar ?: false)
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
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
                                NavEntry(key = key) { SharedElementEntry { HomeScreen() } }
                            }

                            is AppRoute.Routine -> {
                                NavEntry(
                                    key = key,
                                    metadata = noTransitionMetadata()
                                ) {
                                    SharedElementEntry { RoutineScreen(id = key.routineId) }
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
                                    )
                                }
                            }

                            else -> error("Unknown NavKey: $key")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Bridges the per-destination [LocalNavAnimatedContentScope] (only valid inside a [NavEntry]) into
 * the nullable [LocalCardAnimatedContentScope] so shared-element cards can animate between screens.
 */
@Composable
private fun SharedElementEntry(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalCardAnimatedContentScope provides LocalNavAnimatedContentScope.current,
    ) {
        content()
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

private fun noTransitionMetadata() =
    NavDisplay.transitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
            NavDisplay.popTransitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
            NavDisplay.predictivePopTransitionSpec { EnterTransition.None togetherWith ExitTransition.None }