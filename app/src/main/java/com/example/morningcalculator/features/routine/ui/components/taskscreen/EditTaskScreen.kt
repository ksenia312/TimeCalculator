package com.example.morningcalculator.features.routine.ui.components.taskscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskUpdateRequest
import kotlin.time.Duration.Companion.minutes

@Composable
fun EditTaskScreen(
    onConfirm: (TaskUpdateRequest, Int?) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    initialTask: Task,
    initialSubDataId: String?,
    deleteIcon: @Composable () -> Unit,
) {
    var title by remember { mutableStateOf(initialTask.title) }
    val initialIndex = remember(initialTask, initialSubDataId) {
        if (initialSubDataId == null) {
            0
        } else {
            initialTask.data
                .indexOfFirst { it.id == initialSubDataId }
                .takeIf { it >= 0 }
                ?: 0
        }
    }
    var selectedIndex by remember { mutableIntStateOf(initialIndex) }

    val subData = remember {
        (initialTask.data as List<SubData?>).toMutableStateList()
    }

    TaskEditorDialogScaffold(
        screenTitle = "Update task",
        onDismiss = onDismiss,
        headerActions = {
            IconButton(
                onClick = {
                    onDelete()
                    onDismiss()
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
                    selectable = true,
                    selected = selectedIndex == index,
                    onSelect = { selectedIndex = index },
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
                        selectedIndex = selectedIndexAfterRemove(
                            current = selectedIndex,
                            removedIndex = index,
                            newLastIndex = subData.lastIndex,
                        ) ?: 0
                    },
                )
            }

            item {
                AddDurationButton(
                    text = "Add more durations",
                    onClick = {
                        subData.add(null)
                        selectedIndex = subData.lastIndex
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
}