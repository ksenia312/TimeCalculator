package com.xenikii.timecalculator.features.tasks.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.features.tasks.presentation.TasksListViewState
import com.xenikii.timecalculator.features.tasks.ui.components.TasksLazyList
import com.xenikii.timecalculator.shared.components.HomeEmptyState
import com.xenikii.timecalculator.shared.extensions.bottomIndent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListContent(
    viewState: TasksListViewState,
    selectedIds: Set<String>,
    onLongPress: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onEditTask: (Task) -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize()) {
        when (viewState) {
            is TasksListViewState.Loading -> {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .bottomIndent()
                )
            }

            is TasksListViewState.Success -> {
                val tasks = viewState.sorted
                if (tasks.isEmpty()) {
                    HomeEmptyState(
                        title = stringResource(R.string.tasks_empty_title),
                        subtitle = stringResource(R.string.tasks_empty_subtitle),
                        actionText = stringResource(R.string.tasks_empty_action),
                        onActionClick = onCreateTaskClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .bottomIndent()
                            .padding(horizontal = 24.dp)
                    )
                } else {
                    TasksLazyList(
                        tasks = tasks,
                        selectedIds = selectedIds,
                        onLongPress = onLongPress,
                        onToggleSelect = onToggleSelect,
                        onEditTask = onEditTask,
                    )
                }
            }

            is TasksListViewState.Error -> {
                Text(
                    text = viewState.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .bottomIndent()
                )
            }
        }
    }
}
