package com.xenikii.timecalculator.features.routine.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.features.routine.presentation.RoutineViewModel
import com.xenikii.timecalculator.features.routine.ui.components.tasksselection.CopyFromRoutineBottomSheet
import com.xenikii.timecalculator.features.routine.ui.components.tasksselection.TasksBottomSheet
import com.xenikii.timecalculator.shared.components.FabActionButton
import com.xenikii.timecalculator.shared.components.FabItem
import com.xenikii.timecalculator.shared.components.FabMenu
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator

@Composable
fun EditRoutineFloatingButton(
    routine: Routine,
    viewModel: RoutineViewModel,
    isEditMode: Boolean,
) {
    val navigator = LocalNavigator.current
    val isBarExpanded = rememberSaveable { mutableStateOf(false) }
    val showTasksSheet = remember { mutableStateOf(false) }
    val showCopyFromRoutineSheet = remember { mutableStateOf(false) }
    val otherRoutines by viewModel.otherRoutines.collectAsState()

    LaunchedEffect(isEditMode) {
        if (isEditMode) isBarExpanded.value = false
    }

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

    if (showCopyFromRoutineSheet.value) {
        CopyFromRoutineBottomSheet(
            routines = otherRoutines,
            onDismiss = { showCopyFromRoutineSheet.value = false },
            onConfirm = { links ->
                viewModel.copyLinksFromRoutine(links)
            },
        )
    }

    // Both states anchored bottom-end so they overlap exactly while fading, whatever their sizes.
    Box(contentAlignment = Alignment.BottomEnd) {
        AnimatedVisibility(visible = !isEditMode, enter = fadeIn(), exit = fadeOut()) {
            EditRoutineFabMenu(
                isExpanded = isBarExpanded.value,
                onChangeExpanded = { isBarExpanded.value = it },
                onAddTask = { navigator.navigateTo(AppRoute.CreateTask(routineId = routine.id)) },
                onManageTasks = { showTasksSheet.value = true },
                onCopyFromRoutine = { showCopyFromRoutineSheet.value = true },
            )
        }
        AnimatedVisibility(visible = isEditMode, enter = fadeIn(), exit = fadeOut()) {
            FabActionButton(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.content_desc_finish_editing),
                onClick = viewModel::clearSelection,
            )
        }
    }
}

@Composable
private fun EditRoutineFabMenu(
    isExpanded: Boolean,
    onChangeExpanded: (Boolean) -> Unit,
    onAddTask: () -> Unit,
    onManageTasks: () -> Unit,
    onCopyFromRoutine: () -> Unit,
) {
    FabMenu(
        isExpanded = isExpanded,
        onChangeExpanded = onChangeExpanded,
        mainImageVector = Icons.Default.Edit,
        fabItems = listOf(
            FabItem(
                title = stringResource(R.string.fab_add_task),
                iconRes = R.drawable.task,
                contentDescription = stringResource(R.string.fab_add_task),
                onClick = onAddTask),
            FabItem(
                title = stringResource(R.string.fab_manage_tasks),
                iconRes = R.drawable.link,
                contentDescription = stringResource(R.string.fab_manage_tasks),
                onClick = onManageTasks),
            FabItem(
                title = stringResource(R.string.fab_copy_from_routine),
                iconRes = R.drawable.routine,
                contentDescription = stringResource(R.string.fab_copy_from_routine),
                onClick = onCopyFromRoutine),
        )
    )
}
