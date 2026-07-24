package com.xenikii.timecalculator.features.tasks.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.features.tasks.presentation.TasksListViewModel
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.EditTaskArguments
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    onCreateTaskClick: () -> Unit = {},
    viewModel: TasksListViewModel = koinViewModel()
) {
    val navigator = LocalNavigator.current
    val viewState = viewModel.viewState.collectAsStateWithLifecycle()

    TasksListContent(
        viewState = viewState.value,
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
