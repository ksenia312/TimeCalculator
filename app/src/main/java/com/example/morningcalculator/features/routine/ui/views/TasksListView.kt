package com.example.morningcalculator.features.routine.ui.views

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine.Full
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.features.routine.ui.views.task_dialog.EditTaskScreen
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import com.example.morningcalculator.shared.extensions.whenToGetUp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.TasksListView(
    full: Full, viewModel: RoutineViewModel
) {
    val whenToGetUp = full.whenToGetUp()
    val taskPairs = remember(full) { full.data.toMutableStateList() }
    val draggingIndex = remember { mutableStateOf<Int?>(null) }
    val dragOffsetY = remember { mutableFloatStateOf(0f) }
    val editingTask = remember { mutableStateOf<Pair<Task, String>?>(null) }

    if (editingTask.value != null) {
        val task = editingTask.value!!
        EditTaskScreen(
            initialTask = task.first,
            initialSubDataId = task.second,
            onDismiss = { editingTask.value = null },
            onDelete = { viewModel.deleteTask(task.first.id) },
            onConfirm = { request, selectedIndex ->
                viewModel.editTask(
                    request, selectedIndex
                )
            },
        )
    }

    LazyColumn(modifier = Modifier.weight(1f)) {
        item { Spacer(Modifier.height(16.dp)) }
        item(key = "wakeUp") {
            CurrentTimeRow(whenToGetUp.toString(), isTitle = true)
        }
        itemsIndexed(
            items = taskPairs,
            key = { _, (task, _) -> task.id }) { index, (task, selectedSubData) ->
            RoutineTaskItem(
                task = task,
                selectedSubData = selectedSubData,
                index = index,
                full = full,
                viewModel = viewModel,
                draggingIndex = draggingIndex,
                dragOffsetY = dragOffsetY,
                taskPairs = taskPairs,
                editingTask = editingTask
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
