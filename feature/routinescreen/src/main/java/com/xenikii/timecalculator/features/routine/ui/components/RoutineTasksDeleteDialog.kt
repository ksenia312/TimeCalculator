package com.xenikii.timecalculator.features.routine.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.shared.components.DeleteConfirmationDialog

@Composable
fun RoutineTasksDeleteDialog(
    routine: Routine,
    selectedIds: Set<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val titles by remember(routine, selectedIds) {
        derivedStateOf {
            routine.data.filter { it.id in selectedIds }.map { it.task.title }
        }
    }

    DeleteConfirmationDialog(
        title = if (titles.size == 1) {
            stringResource(R.string.routine_tasks_remove_single_title, titles.first())
        } else {
            stringResource(R.string.routine_tasks_remove_multiple_title, titles.size)
        },
        message = if (titles.size == 1) {
            stringResource(R.string.routine_tasks_remove_single_message)
        } else {
            stringResource(R.string.routine_tasks_remove_multiple_message, titles.joinToString(", "))
        },
        confirmText = stringResource(R.string.action_remove),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
