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
import com.example.morningcalculator.core.model.RoutineFullLink
import com.example.morningcalculator.features.routine.ui.views.task_dialog.EditTaskScreen
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import com.example.morningcalculator.shared.extensions.whenToGetUp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.TasksListView(
    full: Full, viewModel: RoutineViewModel
) {
    val whenToGetUp = full.whenToGetUp()
    val fullLinks = remember(full) { full.data.toMutableStateList() }
    val draggingIndex = remember { mutableStateOf<Int?>(null) }
    val dragOffsetY = remember { mutableFloatStateOf(0f) }
    val editingLink = remember { mutableStateOf<RoutineFullLink?>(null) }

    if (editingLink.value != null) {
        val link = editingLink.value!!
        EditTaskScreen(
            initialTask = link.task,
            initialSubDataId = link.subData.id,
            onDismiss = { editingLink.value = null },
            onDelete = { viewModel.deleteTask(link.id) },
            onConfirm = { request, selectedIndex ->
                viewModel.editTask(
                    request, selectedIndex ?: 0, linkId = link.id
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
            items = fullLinks, key = { _, link -> link.id }) { index, link ->
            RoutineTaskItem(
                routineFullLinks = fullLinks,
                index = index,
                full = full,
                viewModel = viewModel,
                draggingIndex = draggingIndex,
                dragOffsetY = dragOffsetY,
                editingLink = editingLink,
                linkFull = link
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
