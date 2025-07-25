package com.example.morningcalculator.shared.components

import android.app.TimePickerDialog
import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    var time = remember(initialTime) {
        mutableStateOf(initialTime)
    }
    val displayText = remember(time) {
        derivedStateOf { time.value.toJavaLocalTime().format(fmt) }
    }

    // один InteractionSource и для TextField, и для клика

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
                    showTimePicker(context, time.value) { picked ->
                        onTimeChange(picked)
                        time.value = picked
                    }
                    true
                } else {
                    false
                }
            },
    )
}

private fun showTimePicker(
    context: Context,
    initial: LocalTime,
    onPicked: (LocalTime) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(LocalTime(hour, minute)) },
        initial.hour,
        initial.minute,
        true         // 24-hour format
    ).show()
}