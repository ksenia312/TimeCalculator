package com.example.morningcalculator.features.taskeditor.ui

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
import com.example.morningcalculator.R
import com.example.morningcalculator.domain.model.SubData
import com.example.morningcalculator.features.taskeditor.presentation.EditTaskViewModel
import com.example.morningcalculator.features.taskeditor.presentation.EditTaskViewState
import com.example.morningcalculator.shared.features.AddDurationButton
import com.example.morningcalculator.shared.features.DurationRow
import com.example.morningcalculator.shared.features.EditorScreenScaffold
import com.example.morningcalculator.shared.features.SaveTaskButton
import com.example.morningcalculator.shared.features.TaskNameField
import com.example.morningcalculator.shared.features.selectedIndexAfterRemove
import com.example.morningcalculator.shared.navigator.EditTaskArguments
import com.example.morningcalculator.shared.navigator.LocalNavigator
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
            val subData = remember(task.id) {
                (task.data as List<SubData?>).toMutableStateList()
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

                    itemsIndexed(subData) { index, item ->
                        val value = item?.duration?.inWholeMinutes?.toString() ?: ""

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
                                if (hasRoutine) {
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
                                if (hasRoutine) {
                                    selectedIndex = subData.lastIndex
                                }
                            },
                        )
                    }

                    item {
                        SaveTaskButton(
                            enabled = subData.isNotEmpty() && subData.all { it != null },
                            onConfirm = {
                                viewModel.saveTask(
                                    title = title,
                                    subData = subData.filterNotNull(),
                                    selectedDurationIndex = selectedIndex,
                                )
                            },
                            onDismiss = navigator::navigateBack,
                        )
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
