package com.xenikii.timecalculator.shared.navigator

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.xenikii.timecalculator.app.presentation.AuthViewState
import com.xenikii.timecalculator.app.presentation.MainViewModel
import com.xenikii.timecalculator.features.auth.ui.LoginScreen
import com.xenikii.timecalculator.features.auth.ui.RegisterScreen
import com.xenikii.timecalculator.features.auth.ui.WelcomeScreen
import com.xenikii.timecalculator.features.home.ui.HomeScreen
import com.xenikii.timecalculator.features.onboarding.ui.OnboardingScreen
import com.xenikii.timecalculator.features.routine.ui.RoutineScreen
import com.xenikii.timecalculator.features.routineeditor.ui.CreateRoutineScreen
import com.xenikii.timecalculator.features.routineeditor.ui.EditRoutineScreen
import com.xenikii.timecalculator.features.taskeditor.ui.CreateTaskScreen
import com.xenikii.timecalculator.features.taskeditor.ui.EditTaskScreen
import com.xenikii.timecalculator.shared.animation.LocalCardAnimatedContentScope
import com.xenikii.timecalculator.shared.animation.LocalSharedTransitionScope
import com.xenikii.timecalculator.shared.theme.SetStatusBarForBrightTopBar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigator(
    initialBackStack: List<NavKey> = listOf(AppRoute.Welcome),
    onBackStackCreated: (NavBackStack<NavKey>) -> Unit = {},
) {
    AppNavigatorProvider(
        initialBackStack = initialBackStack,
        onBackStackCreated = onBackStackCreated,
    ) {
        val backStack = LocalNavigationBackStack.current
        val currentRoute = backStack.lastOrNull() as? AppRoute
        SetStatusBarForBrightTopBar(hasBrightTopBar = currentRoute?.hasBrightTopBar ?: false)

        val mainViewModel: MainViewModel = koinViewModel()
        val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()
        val pendingDeepLink by PendingDeepLink.route.collectAsStateWithLifecycle()

        LaunchedEffect(mainState.authViewState) {
            val current = backStack.lastOrNull() as? AppRoute
            when (mainState.authViewState) {
                AuthViewState.Initializing -> Unit

                is AuthViewState.LoggedOut -> {
                    if (current == null || current.requiresAuthentication() || current is AppRoute.Welcome) {
                        val targetRoute = if (mainViewModel.isOnboardingCompleted()) {
                            AppRoute.Welcome
                        } else {
                            AppRoute.Onboarding
                        }
                        backStack.resetTo(targetRoute)
                    }
                }

                AuthViewState.LoggedIn -> {
                    if (current == null ||
                        current == AppRoute.Welcome ||
                        current == AppRoute.Login ||
                        current == AppRoute.Register
                    ) {
                        backStack.resetTo(AppRoute.Home)
                    }
                }
            }
        }


        LaunchedEffect(mainState.authViewState, pendingDeepLink) {
            if (mainState.authViewState == AuthViewState.LoggedIn) {
                pendingDeepLink?.let { route ->
                    if (backStack.lastOrNull() != route) {
                        backStack.add(route)
                    }
                    PendingDeepLink.route.value = null
                }
            }
        }

        if (mainState.authViewState == AuthViewState.Initializing) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
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
                                AppRoute.Onboarding -> {
                                    NavEntry(key = key) {
                                        OnboardingScreen(
                                            onFinished = { backStack.resetTo(AppRoute.Welcome) },
                                        )
                                    }
                                }

                                AppRoute.Welcome -> {
                                    NavEntry(key = key) { WelcomeScreen() }
                                }

                                AppRoute.Login -> {
                                    NavEntry(key = key) { LoginScreen() }
                                }

                                AppRoute.Register -> {
                                    NavEntry(key = key) { RegisterScreen() }
                                }

                                AppRoute.Home -> {
                                    NavEntry(key = key) { SharedElementEntry { HomeScreen() } }
                                }

                                is AppRoute.Routine -> {
                                    NavEntry(key = key) {
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
}

private fun NavBackStack<NavKey>.resetTo(route: AppRoute) {
    clear()
    add(route)
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