package com.xenikii.timecalculator.features.home.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListState
import com.xenikii.timecalculator.features.tasks.presentation.TasksListViewState
import com.xenikii.timecalculator.shared.components.DeleteConfirmationDialog

@Composable
internal fun HomeDeleteDialog(
    selectedTab: HomeTab,
    routinesViewState: RoutinesListState,
    routinesSelectedIds: Set<String>,
    tasksViewState: TasksListViewState,
    tasksSelectedIds: Set<String>,
    onConfirmDeleteTasks: () -> Unit,
    onConfirmDeleteRoutines: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isTasksTab = selectedTab == HomeTab.TASKS

    val routineSelectedTitles by remember(routinesViewState, routinesSelectedIds) {
        derivedStateOf {
            if (routinesViewState is RoutinesListState.Success) {
                routinesViewState.items.filter { it.routine.id in routinesSelectedIds }.map { it.routine.title }
            } else emptyList()
        }
    }

    val taskSelectedTitles by remember(tasksViewState, tasksSelectedIds) {
        derivedStateOf {
            if (tasksViewState is TasksListViewState.Success) {
                tasksViewState.sorted.filter { it.id in tasksSelectedIds }.map { it.title }
            } else emptyList()
        }
    }

    val titles = if (isTasksTab) taskSelectedTitles else routineSelectedTitles
    val onConfirm = if (isTasksTab) onConfirmDeleteTasks else onConfirmDeleteRoutines

    val singleTitleRes = if (isTasksTab) R.string.tasks_delete_single_title else R.string.routines_delete_single_title
    val multipleTitleRes = if (isTasksTab) R.string.tasks_delete_multiple_title else R.string.routines_delete_multiple_title
    val singleMsgRes = if (isTasksTab) R.string.tasks_delete_single_message else R.string.routines_delete_single_message
    val multipleMsgRes = if (isTasksTab) R.string.tasks_delete_multiple_message else R.string.routines_delete_multiple_message

    DeleteConfirmationDialog(
        title = if (titles.size == 1) {
            stringResource(singleTitleRes, titles.first())
        } else {
            stringResource(multipleTitleRes, titles.size)
        },
        message = if (titles.size == 1) {
            stringResource(singleMsgRes)
        } else {
            stringResource(multipleMsgRes, titles.joinToString(", "))
        },
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
