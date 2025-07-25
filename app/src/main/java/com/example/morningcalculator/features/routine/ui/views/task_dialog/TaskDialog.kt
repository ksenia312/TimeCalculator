package com.example.morningcalculator.features.routine.ui.views.task_dialog

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun <T> TaskDialog(
    dialogTitle: String,
    data: List<T>,
    initialIndex: Int,
    initialTitle: String,
    toInputValues: (List<T>) -> List<String>,
    onValueChange: (T, String) -> T,
    confirmEnabled: (List<T>) -> Boolean,
    onConfirm: (String, List<T>, Int) -> Unit,
    onDismiss: () -> Unit,
    newElement: T
) {
    var title by remember { mutableStateOf(initialTitle) }
    var selectedIndex by remember { mutableIntStateOf(initialIndex) }
    val scrollState = rememberScrollState()
    val mutableData = remember { data.toMutableStateList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ConfirmButton(
                enabled = confirmEnabled(mutableData), onConfirm = {
                    onConfirm(
                        title, mutableData, selectedIndex
                    )
                }, onDismiss = onDismiss
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(dialogTitle) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollable(scrollState, orientation = Orientation.Vertical)
            ) {
                NameEditor(title, onValueChange = { title = it })
                Spacer(Modifier.height(12.dp))
                DurationsList(
                    numbers = toInputValues(mutableData),
                    selectedIndex = selectedIndex,
                    onRemove = { index ->
                        mutableData.removeAt(index)
                        selectedIndex = mutableData.lastIndex
                    },
                    onValueChange = { index, new ->
                        mutableData[index] = onValueChange(
                            mutableData[index], new
                        )
                    },
                    onSelectedIndexChange = { selectedIndex = it })
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        mutableData.add(newElement)
                        selectedIndex = mutableData.lastIndex
                    }, modifier = Modifier.fillMaxWidth()
                ) { Text("Add more durations") }

            }
        })
}

@Composable
private fun ConfirmButton(onConfirm: () -> Unit, enabled: Boolean, onDismiss: () -> Unit) =
    TextButton(enabled = enabled, onClick = {
        onConfirm()
        onDismiss()
    }) { Text("Ok") }

@Composable
private fun NameEditor(
    title: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = title,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Name") })
}

@Composable
private fun DurationsList(
    numbers: List<String>,
    selectedIndex: Int,
    onRemove: (Int) -> Unit,
    onValueChange: (Int, String) -> Unit,
    onSelectedIndexChange: (Int) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(numbers.size) { index ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RadioButton(
                    selected = selectedIndex == index, onClick = { onSelectedIndexChange(index) })
                NumberField(
                    value = numbers[index], onValueChange = { new ->
                        if (new.all { it.isDigit() }) {
                            onValueChange(index, new)
                        }
                    }, label = "Duration №${index + 1}"
                )
                IconButton(onClick = { onRemove(index) }) {
                    Icon(Icons.Default.Close, "Remove")
                }
            }
        }
    }

}

@Composable
private fun RowScope.NumberField(
    value: String, onValueChange: (String) -> Unit, label: String, enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        enabled = enabled,
        onValueChange = onValueChange,
        modifier = Modifier.weight(1f),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}