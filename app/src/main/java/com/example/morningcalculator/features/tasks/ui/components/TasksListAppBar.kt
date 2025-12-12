package com.example.morningcalculator.features.tasks.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.morningcalculator.features.tasks.presentation.TasksListViewState
import com.example.morningcalculator.shared.components.CustomTopBar
import com.example.morningcalculator.shared.components.CustomTopBarHeadingItem
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewConstants
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TasksListAppBar(tasksListViewState: TasksListViewState) {
    val tasksCount = (tasksListViewState as? TasksListViewState.Success)?.tasks?.size
    val accentColor = LocalCustomColorScheme.current.accent

    CustomTopBar(
        accentColor = accentColor,
        onAccentColor = MaterialTheme.colorScheme.onPrimary,
        titleItems = {
            CustomTopBarHeadingItem(
                title = "${tasksCount ?: 0}",
                subtitle = "Tasks",
            )
        },
        actions = {}
    )
}

@PreviewAll
@Composable
private fun HomeAppBarPreview() {
    PreviewTheme {
        TasksListAppBar(
            tasksListViewState = TasksListViewState.Success(
                tasks = PreviewConstants.tasks,
            )
        )
    }
}