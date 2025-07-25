package com.example.morningcalculator.features.home.ui.views


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import com.example.morningcalculator.shared.components.TimePickerField
import kotlinx.datetime.LocalTime

@Composable
fun RoutineDialog(
    onConfirm: (RoutineRequest) -> Unit, onDismiss: () -> Unit,
    initialRoutine: Routine? = null
) {
    var title by remember { mutableStateOf(initialRoutine?.title ?: "") }
    var time by remember { mutableStateOf(initialRoutine?.time ?: LocalTime(7, 0)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = {
                onConfirm(
                    RoutineRequest(title = title, time = time)
                )
                onDismiss()
            }) { Text("Ok") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add new routine") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") })

                Spacer(Modifier.height(12.dp))
                TimePickerField(
                    label = "When to leave?", initialTime = time, onTimeChange = { time = it })

            }
        })
}
