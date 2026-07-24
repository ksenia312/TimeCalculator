package com.xenikii.timecalculator.features.taskeditor.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.taskeditor.presentation.EditTaskViewModel
import com.xenikii.timecalculator.features.taskeditor.presentation.EditTaskViewState
import com.xenikii.timecalculator.shared.features.AddDurationButton
import com.xenikii.timecalculator.shared.features.DurationRow
import com.xenikii.timecalculator.shared.features.EditorScreenScaffold
import com.xenikii.timecalculator.shared.features.SaveTaskButton
import com.xenikii.timecalculator.shared.features.TaskNameField
import com.xenikii.timecalculator.shared.features.selectedIndexAfterRemove
import com.xenikii.timecalculator.shared.navigator.EditTaskArguments
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.minutes

@Composable
fun EditTaskScreen(
    arguments: EditTaskArguments,
    viewModel: EditTaskViewModel = koinViewModel(
        parameters = { parametersOf(arguments) }
    ),
) {
    val navigator = LocalNavigator.current
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val hasRoutine = viewModel.hasRoutine
    val showDuplicateError by viewModel.showDuplicateError.collectAsStateWithLifecycle()

    when (val state = viewState) {
        EditTaskViewState.Loading -> {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        EditTaskViewState.Error -> {
            EditorScreenScaffold(
                screenTitle = stringResource(R.string.task_update_title),
                onDismiss = navigator::navigateBack,
            ) { padding ->
                Text(
                    text = stringResource(R.string.top_bar_error),
                    modifier = Modifier.padding(padding).padding(16.dp),
                )
            }
        }

        is EditTaskViewState.Success -> {
            val task = state.task
            var title by remember(task.id) { mutableStateOf(task.title) }
            var selectedIndex by remember(task.id, hasRoutine) {
                mutableStateOf(if (hasRoutine) state.initialSelectedIndex else null)
            }
            val durations = remember(task.id) {
                task.data.map { it.duration.inWholeMinutes.toString() }.toMutableStateList()
            }
            var showDeleteConfirmation by remember(task.id) { mutableStateOf(false) }

            EditorScreenScaffold(
                screenTitle = stringResource(R.string.task_update_title),
                onDismiss = navigator::navigateBack,
                headerActions = {
                    IconButton(
                        onClick = {
                            showDeleteConfirmation = true
                        },
                    ) {
                        if (hasRoutine) {
                            Image(
                                painter = painterResource(R.drawable.unlink),
                                contentDescription = stringResource(R.string.content_desc_delete),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                            )
                        } else {
                            Image(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.content_desc_delete),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                            )
                        }
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

                    itemsIndexed(durations) { index, value ->
                        DurationRow(
                            index = index,
                            value = value,
                            selectable = hasRoutine,
                            selected = selectedIndex == index,
                            onSelect = {
                                if (hasRoutine) {
                                    selectedIndex = index
                                }
                            },
                            onValueChange = { new ->
                                if (new.all { it.isDigit() }) {
                                    durations[index] = new
                                }
                            },
                            onRemove = {
                                durations.removeAt(index)
                                if (hasRoutine) {
                                    selectedIndex = selectedIndexAfterRemove(
                                        current = selectedIndex,
                                        removedIndex = index,
                                        newLastIndex = durations.lastIndex,
                                    )
                                }
                            },
                        )
                    }

                    item {
                        AddDurationButton(
                            text = stringResource(R.string.task_add_more_durations),
                            onClick = {
                                durations.add("")
                                if (hasRoutine) {
                                    selectedIndex = durations.lastIndex
                                }
                            },
                        )
                    }

                    item {
                        SaveTaskButton(
                            enabled = durations.isNotEmpty() && durations.all { it.isNotBlank() },
                            onConfirm = {
                                val durationsRes = runCatching {
                                    durations.map { it.toInt().minutes }
                                }.getOrNull()
                                if (durationsRes != null) {
                                    val saved = viewModel.saveTask(
                                        title = title,
                                        durations = durationsRes,
                                        selectedDurationIndex = selectedIndex,
                                    )
                                    if (saved) {
                                        navigator.navigateBack()
                                    }
                                }
                            },
                        )

                        if (showDuplicateError) {
                            Text(
                                text = stringResource(R.string.task_duplicate_durations_error),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }

            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = {
                        Text(
                            stringResource(
                                if (hasRoutine) R.string.task_unlink_dialog_title
                                else R.string.task_delete_dialog_title
                            )
                        )
                    },
                    text = {
                        Text(
                            stringResource(
                                if (hasRoutine) R.string.task_unlink_dialog_message
                                else R.string.task_delete_dialog_message
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = false
                                viewModel.delete()
                                navigator.navigateBack()
                            }
                        ) {
                            Text(
                                text = stringResource(
                                    if (hasRoutine) R.string.action_remove
                                    else R.string.action_delete
                                ),
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
    }
}
