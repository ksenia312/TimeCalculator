package com.example.morningcalculator.features.routine.ui.components.taskscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.TaskRequest
import kotlin.time.Duration.Companion.minutes

@Composable
fun CreateTaskScreen(
    linkedToRoutine: Boolean,
    onConfirm: (TaskRequest, Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    val durations = remember { mutableStateListOf("") }

    val selectable = linkedToRoutine
    var selectedIndex by remember {
        mutableStateOf(if (selectable) 0 else null)
    }

    TaskEditorDialogScaffold(
        screenTitle = "Create task",
        onDismiss = onDismiss,
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
                    autofocus = true,
                    onValueChange = { title = it },
                )
            }

            itemsIndexed(durations) { index, value ->
                DurationRow(
                    index = index,
                    value = value,
                    selectable = selectable,
                    selected = selectedIndex == index,
                    onSelect = { selectedIndex = index },
                    onValueChange = { new ->
                        if (new.all { it.isDigit() }) {
                            durations[index] = new
                        }
                    },
                    onRemove = {
                        durations.removeAt(index)
                        if (selectable) {
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
                    text = "Add more durations",
                    onClick = {
                        durations.add("")
                        if (selectable) {
                            selectedIndex = durations.lastIndex
                        }
                    },
                )
            }

            item {
                SaveTaskButton(
                    enabled = durations.isNotEmpty() && durations.all { it.isNotBlank() },
                    onConfirm = {
                        val durationsRes = runCatching { durations.map { it.toInt().minutes } }.getOrNull()
                        if (durationsRes != null) {
                            onConfirm(
                                TaskRequest(
                                    title = title,
                                    description = "",
                                    durations = durationsRes
                                ),
                                selectedIndex,
                            )
                        }
                    },
                    onDismiss = onDismiss,
                )
            }
        }
    }
}