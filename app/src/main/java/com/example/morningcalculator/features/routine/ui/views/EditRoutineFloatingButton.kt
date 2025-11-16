package com.example.morningcalculator.features.routine.ui.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.routine.ui.views.task_dialog.CreateTaskScreen
import com.example.morningcalculator.features.routine.ui.views.tasks_bottom_sheet.TasksBottomSheet
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import com.example.morningcalculator.shared.components.FabItem
import com.example.morningcalculator.shared.components.FabMenu

@Composable
fun EditRoutineFloatingButton(
    routine: Routine.Full,
    viewModel: RoutineViewModel,
) {
    val isBarExpanded = rememberSaveable { mutableStateOf(false) }
    val showTasksSheet = remember { mutableStateOf(false) }
    val showAddTaskDialog = remember { mutableStateOf(false) }

    if (showTasksSheet.value) {
        TasksBottomSheet(
            routine = routine,
            onDismiss = { showTasksSheet.value = false },
            onShowAddTasksDialog = { showAddTaskDialog.value = true },
            viewModel = viewModel
        )
    }
    if (showAddTaskDialog.value) {
        CreateTaskScreen(
            linkedToRoutine = true,
            onConfirm = { request, selectedIndex ->
            viewModel.addNewTask(request, selectedIndex ?: 0)
            showAddTaskDialog.value = false
        }, onDismiss = { showAddTaskDialog.value = false }
        )
    }

    FabMenu(
        isExpanded = isBarExpanded.value,
        onChangeExpanded = { isBarExpanded.value = it },
        mainImageVector = Icons.Default.Edit,
        fabItems = listOf(
            FabItem(
                title = "Add Task",
                icon = Icons.Default.Add,
                contentDescription = "Add Task",
                onClick = {
                    showAddTaskDialog.value = true
                }),
            FabItem(
                title = "Manage Tasks",
                icon = Icons.Default.Search,
                contentDescription = "Manage Tasks",
                onClick = {
                    showTasksSheet.value = true
                }),
        )
    )
}