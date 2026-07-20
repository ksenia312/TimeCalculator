package com.example.morningcalculator.features.tasks.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.features.tasks.presentation.TasksListViewState
import com.example.morningcalculator.features.tasks.ui.components.TaskListItem
import com.example.morningcalculator.shared.components.HomeEmptyState
import com.example.morningcalculator.shared.extensions.bottomIndent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListContent(
    viewState: TasksListViewState,
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
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        item { Spacer(Modifier.height(16.dp)) }
                        tasks.forEach { task ->
                            item(key = task.id) {
                                TaskListItem(task) {
                                    onEditTask(task)
                                }
                            }
                        }
                        item { Box(Modifier.bottomIndent()) }
                    }
                }
            }

            is TasksListViewState.Error -> {
                val viewState = viewState
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
