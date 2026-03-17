package com.example.morningcalculator.features.tasks.ui

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ColorFilter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.features.routine.ui.components.taskscreen.EditTaskScreen
import com.example.morningcalculator.features.tasks.presentation.TasksListViewModel
import com.example.morningcalculator.shared.components.AppScaffold
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(viewModel: TasksListViewModel = koinViewModel()) {
    val viewState = viewModel.viewState.collectAsStateWithLifecycle()
    val editingTask = remember { mutableStateOf<Task?>(null) }

    editingTask.value?.let {
        EditTaskScreen(
            initialTask = it,
            initialSubDataId = null,
            onDismiss = { editingTask.value = null },
            onDelete = { viewModel.deleteTask(it.id) },
            deleteIcon = {
                Image(
                    Icons.Default.DeleteOutline,
                    contentDescription = "delete",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
                )
            },
            onConfirm = { request, _ ->
                viewModel.editTask(request)
            },
        )
    }

    TasksListContent(
        viewState = viewState.value,
        onEditTask = { task -> editingTask.value = task }
    )
}
