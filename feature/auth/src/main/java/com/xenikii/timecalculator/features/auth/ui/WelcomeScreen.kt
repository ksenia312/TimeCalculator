package com.xenikii.timecalculator.features.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.features.auth.presentation.WelcomeViewModel
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel

@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    WelcomeContent(
        state = state,
        onNavigateToLogin = { navigator.navigateTo(AppRoute.Login) },
        onNavigateToRegister = { navigator.navigateTo(AppRoute.Register) },
    )
}
