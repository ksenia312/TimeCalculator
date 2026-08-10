package com.xenikii.timecalculator.features.settings.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.features.settings.presentation.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val viewState = viewModel.viewState.collectAsStateWithLifecycle()
    SettingsContent(
        viewState = viewState.value,
        onLogoutClick = viewModel::logout,
    )
}
