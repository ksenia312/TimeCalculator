package com.example.morningcalculator.features.routine.ui.views.task_dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskUpdateRequest
import kotlin.time.Duration.Companion.minutes


@Composable
fun EditTaskScreen(
    onConfirm: (TaskUpdateRequest, Int) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    initialTask: Task,
    initialSubDataId: String,
) {
    TaskScreen(
        screenTitle = "Update task",
        data = initialTask.data as List<SubData?>,
        initialIndex = initialTask.data.indexOfFirst { it.id == initialSubDataId },
        initialTitle = initialTask.title,
        newElement = null,
        toInputValues = {
            it.map { data ->
                data?.duration?.inWholeMinutes?.toString() ?: ""
            }
        },
        onValueChange = { current, new ->
            val duration = new.toIntOrNull()?.minutes
            var result = current
            if (duration != null) {
                result = current?.copy(duration = duration) ?: SubData(duration = duration)
            }
            result
        },
        confirmEnabled = { data -> data.all { it != null } && data.isNotEmpty() },
        onConfirm = { title, data, selectedIndex ->
            onConfirm(
                TaskUpdateRequest(
                    taskId = initialTask.id,
                    description = initialTask.description,
                    title = title,
                    subData = data.filterNotNull(),
                ), selectedIndex
            )
        },
        headerActions = {
            IconButton(onClick = {
                onDelete()
                onDismiss()
            }) {
                Icon(Icons.Outlined.Delete, "Delete")
            }
        },
        onDismiss = onDismiss
    )
}