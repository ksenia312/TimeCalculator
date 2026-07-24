package com.xenikii.timecalculator.features.routineslist.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesListScreen(
    onCreateRoutineClick: () -> Unit = {},
    routinesViewModel: RoutinesListViewModel = koinViewModel()
) {
    val viewState = routinesViewModel.viewState.collectAsStateWithLifecycle()

    RoutinesListContent(
        viewState = viewState.value,
        onCreateRoutineClick = onCreateRoutineClick,
    )
}
