package com.example.morningcalculator.features.routine.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.routine.presentation.RoutineViewModel
import com.example.morningcalculator.shared.navigator.AppRoute
import com.example.morningcalculator.shared.navigator.LocalNavigator
import com.example.morningcalculator.features.routine.ui.components.tasksselection.TasksBottomSheet
import com.example.morningcalculator.shared.components.FabItem
import com.example.morningcalculator.shared.components.FabMenu

@Composable
fun EditRoutineFloatingButton(
    routine: Routine,
    viewModel: RoutineViewModel,
) {
    val navigator = LocalNavigator.current
    val isBarExpanded = rememberSaveable { mutableStateOf(false) }
    val showTasksSheet = remember { mutableStateOf(false) }

    if (showTasksSheet.value) {
        TasksBottomSheet(
            routine = routine,
            onDismiss = { showTasksSheet.value = false },
            onShowAddTasksDialog = {
                navigator.navigateTo(AppRoute.CreateTask(routineId = routine.id))
            },
            viewModel = viewModel
        )
    }

    FabMenu(
        isExpanded = isBarExpanded.value,
        onChangeExpanded = { isBarExpanded.value = it },
        mainImageVector = Icons.Default.Edit,
        fabItems = listOf(
            FabItem(
                title = stringResource(R.string.fab_add_task),
                iconRes = R.drawable.task,
                contentDescription = stringResource(R.string.fab_add_task),
                onClick = {
                    navigator.navigateTo(AppRoute.CreateTask(routineId = routine.id))
                }),
            FabItem(
                title = stringResource(R.string.fab_manage_tasks),
                iconRes = R.drawable.link,
                contentDescription = stringResource(R.string.fab_manage_tasks),
                onClick = {
                    showTasksSheet.value = true
                }),
        )
    )
}