package com.example.morningcalculator.features.routine.ui.views.task_dialog

import androidx.compose.runtime.Composable
import com.example.morningcalculator.core.model.TaskRequest
import kotlin.time.Duration.Companion.minutes


@Composable
fun AddTaskScreen(
    onConfirm: (TaskRequest, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    TaskScreen(
        screenTitle = "Add task",
        data = listOf(""),
        initialIndex = 0,
        initialTitle = "",
        toInputValues = { it },
        newElement = "",
        onValueChange = { current, new -> new },
        confirmEnabled = { data ->
            data.all { it.isNotBlank() } && data.isNotEmpty() },
        onConfirm = { title, data, selectedIndex ->
            onConfirm(
                TaskRequest(title, "", data.map { it.toInt().minutes }), selectedIndex
            )
        },
        onDismiss = onDismiss
    )
}