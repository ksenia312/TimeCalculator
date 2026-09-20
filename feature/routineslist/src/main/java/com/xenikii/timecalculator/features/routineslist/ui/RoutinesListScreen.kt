package com.xenikii.timecalculator.features.routineslist.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListState
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListViewModel
import com.xenikii.timecalculator.shared.components.RoutineActiveLimitDialog
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
    val showLimitDialog by routinesViewModel.showLimitDialog.collectAsStateWithLifecycle()

    val state = viewState.value
    val hasData = state is RoutinesListState.Success && state.items.isNotEmpty()

    RoutinesListContent(
        viewState = if (isSyncing && !hasData) RoutinesListState.Loading else state,
        selectedIds = selectedIds.value,
        onLongPress = routinesViewModel::toggleSelection,
        onToggleSelect = routinesViewModel::toggleSelection,
        onTogglePause = routinesViewModel::togglePause,
        onCreateRoutineClick = onCreateRoutineClick,
    )

    if (showLimitDialog) {
        RoutineActiveLimitDialog(onDismiss = routinesViewModel::dismissLimitDialog)
    }
}
