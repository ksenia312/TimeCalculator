package com.example.morningcalculator.features.routine.ui.views.task_dialog

import androidx.compose.runtime.Composable
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskUpdateRequest
import kotlin.time.Duration.Companion.minutes


@Composable
fun EditTaskDialog(
    onConfirm: (TaskUpdateRequest, Int) -> Unit,
    onDismiss: () -> Unit,
    initialTask: Task,
    initialSubDataId: String,
) {
    TaskDialog(
        dialogTitle = "Update task",
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
            println("on confirm update $title $data")
            onConfirm(
                TaskUpdateRequest(
                    taskId = initialTask.id,
                    description = initialTask.description,
                    title = title,
                    subData = data.filterNotNull(),
                ), selectedIndex
            )
        },
        onDismiss = onDismiss
    )
}