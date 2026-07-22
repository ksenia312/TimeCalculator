package com.example.morningcalculator.features.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.morningcalculator.features.auth.presentation.WelcomeViewModel
import com.example.morningcalculator.shared.navigator.AppRoute
import com.example.morningcalculator.shared.navigator.LocalNavigator
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
