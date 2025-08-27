package com.example.morningcalculator.shared.components

import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimePickerField(
    modifier: Modifier = Modifier,
    label: String = "When to leave?",
    initialTime: LocalTime = LocalTime(9, 0),
    onTimeChange: (LocalTime) -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    val time = remember(initialTime) {
        mutableStateOf(initialTime)
    }
    val showDialog = remember { mutableStateOf(false) }
    val displayText = remember(time) {
        derivedStateOf { time.value.toJavaLocalTime().format(fmt) }
    }

    if (showDialog.value) {
        TimePickerDialogCompose(
            initial = time.value,
            onDismiss = { showDialog.value = false },
            onConfirm = {
                time.value = it
                onTimeChange(it)
                showDialog.value = false
            }
        )
    }

    OutlinedTextField(
        value = displayText.value,
        onValueChange = {},
        readOnly = true,
        interactionSource = null,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .pointerInteropFilter { event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    showDialog.value = true
                    true
                } else {
                    false
                }
            },
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogCompose(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime(state.hour, state.minute))
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = state)
        }
    )
}