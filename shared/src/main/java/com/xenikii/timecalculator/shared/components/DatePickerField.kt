package com.xenikii.timecalculator.shared.components

import android.content.res.Configuration
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun DatePickerField(
    modifier: Modifier = Modifier,
    label: String? = null,
    initialDate: LocalDate = LocalDate.now(),
    onDateChange: (LocalDate) -> Unit
) {
    val resolvedLabel = label ?: stringResource(R.string.label_when_to_leave)
    val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val date = remember(initialDate) {
        mutableStateOf(initialDate)
    }
    val showDialog = remember { mutableStateOf(false) }
    val displayText = remember(date) {
        derivedStateOf { date.value.format(fmt) }
    }


    if (showDialog.value) {
        val isLandscape = LocalConfiguration.current.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date
                .value
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
            initialDisplayMode = if (isLandscape) DisplayMode.Input else DisplayMode.Picker
        )

        DatePickerDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            date.value = java.time.Instant
                                .ofEpochMilli(selectedMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onDateChange(date.value)
                        }
                        showDialog.value = false
                    }
                ) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AppTextField(
        value = displayText.value,
        onValueChange = {},
        readOnly = true,
        interactionSource = null,
        label = { Text(resolvedLabel) },
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