package com.example.morningcalculator.features.routine.ui.components.taskscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskUpdateRequest
import kotlin.time.Duration.Companion.minutes

@Composable
fun EditTaskScreen(
    canSelectCurrentTask: Boolean,
    onConfirm: (TaskUpdateRequest, Int?) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    initialTask: Task,
    initialSelectedSubDataId: String?,
    deleteIcon: @Composable () -> Unit,
) {
    var title by remember { mutableStateOf(initialTask.title) }
    val initialIndex = remember(initialTask, initialSelectedSubDataId) {
        if (initialSelectedSubDataId == null) {
            0
        } else {
            initialTask.data
                .indexOfFirst { it.id == initialSelectedSubDataId }
                .takeIf { it >= 0 }
                ?: 0
        }
    }
    var selectedIndex by remember(initialIndex, canSelectCurrentTask) {
        mutableStateOf(if (canSelectCurrentTask) initialIndex else null)
    }

    val subData = remember {
        (initialTask.data as List<SubData?>).toMutableStateList()
    }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    EditorDialogScaffold(
        screenTitle = stringResource(R.string.task_update_title),
        onDismiss = onDismiss,
        headerActions = {
            IconButton(
                onClick = {
                    showDeleteConfirmation = true
                },
            ) {
                deleteIcon()
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TaskNameField(
                    title = title,
                    autofocus = false,
                    onValueChange = { title = it },
                )
            }

            itemsIndexed(subData) { index, item ->
                val value = item?.duration?.inWholeMinutes?.toString() ?: ""

                DurationRow(
                    index = index,
                    value = value,
                    selectable = canSelectCurrentTask,
                    selected = selectedIndex == index,
                    onSelect = {
                        if (canSelectCurrentTask) {
                            selectedIndex = index
                        }
                    },
                    onValueChange = { new ->
                        if (new.all { it.isDigit() }) {
                            val duration = new.toIntOrNull()?.minutes
                            if (duration != null) {
                                val current = subData[index]
                                subData[index] = current?.copy(duration = duration)
                                    ?: SubData(duration = duration)
                            }
                        }
                    },
                    onRemove = {
                        subData.removeAt(index)
                        if (canSelectCurrentTask) {
                            selectedIndex = selectedIndexAfterRemove(
                                current = selectedIndex,
                                removedIndex = index,
                                newLastIndex = subData.lastIndex,
                            )
                        }
                    },
                )
            }

            item {
                AddDurationButton(
                    text = stringResource(R.string.task_add_more_durations),
                    onClick = {
                        subData.add(null)
                        if (canSelectCurrentTask) {
                            selectedIndex = subData.lastIndex
                        }
                    },
                )
            }

            item {
                SaveTaskButton(
                    enabled = subData.isNotEmpty() && subData.all { it != null },
                    onConfirm = {
                        onConfirm(
                            TaskUpdateRequest(
                                taskId = initialTask.id,
                                description = initialTask.description,
                                title = title,
                                subData = subData.filterNotNull(),
                            ),
                            selectedIndex,
                        )
                    },
                    onDismiss = onDismiss,
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.task_delete_dialog_title)) },
            text = { Text(stringResource(R.string.task_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}