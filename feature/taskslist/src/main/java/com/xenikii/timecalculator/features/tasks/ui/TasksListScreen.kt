package com.xenikii.timecalculator.features.tasks.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.features.tasks.presentation.TasksListViewState
import com.xenikii.timecalculator.features.tasks.presentation.TasksListViewModel
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.EditTaskArguments
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    onCreateTaskClick: () -> Unit = {},
    isSyncing: Boolean = false,
    viewModel: TasksListViewModel = koinViewModel()
) {
    val navigator = LocalNavigator.current
    val viewState = viewModel.viewState.collectAsStateWithLifecycle()
    val selectedIds = viewModel.selectedIds.collectAsStateWithLifecycle()

    val state = viewState.value
    val hasData = state is TasksListViewState.Success && state.tasks.isNotEmpty()

    TasksListContent(
        viewState = if (isSyncing && !hasData) TasksListViewState.Loading else state,
        selectedIds = selectedIds.value,
        onLongPress = viewModel::toggleSelection,
        onToggleSelect = viewModel::toggleSelection,
        onEditTask = { task ->
            navigator.navigateTo(
                AppRoute.EditTask(
                    arguments = EditTaskArguments(taskId = task.id)
                )
            )
        },
        onCreateTaskClick = onCreateTaskClick,
    )
}
