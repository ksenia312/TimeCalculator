package com.xenikii.timecalculator.features.routineslist.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListState
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesListScreen(
    onCreateRoutineClick: () -> Unit = {},
    isSyncing: Boolean = false,
    routinesViewModel: RoutinesListViewModel = koinViewModel()
) {
    val viewState = routinesViewModel.viewState.collectAsStateWithLifecycle()
    val selectedIds = routinesViewModel.selectedIds.collectAsStateWithLifecycle()

    RoutinesListContent(
        viewState = if (isSyncing) RoutinesListState.Loading else viewState.value,
        selectedIds = selectedIds.value,
        onLongPress = routinesViewModel::toggleSelection,
        onToggleSelect = routinesViewModel::toggleSelection,
        onCreateRoutineClick = onCreateRoutineClick,
    )
}
