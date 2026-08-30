package com.xenikii.timecalculator.features.landing.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.features.landing.presentation.LandingState
import com.xenikii.timecalculator.features.landing.presentation.LandingViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    onCreateRoutineClick: () -> Unit = {},
    isSyncing: Boolean = false,
    landingViewModel: LandingViewModel = koinViewModel(),
) {
    val viewState = landingViewModel.viewState.collectAsStateWithLifecycle()

    LandingContent(
        viewState = if (isSyncing) LandingState.Loading else viewState.value,
        onCreateRoutineClick = onCreateRoutineClick,
    )
}
