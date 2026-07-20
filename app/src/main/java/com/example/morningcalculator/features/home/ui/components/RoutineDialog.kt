package com.example.morningcalculator.features.home.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.RoutineScheduleAnchor
import com.example.morningcalculator.features.routine.ui.components.taskscreen.EditorDialogScaffold
import com.example.morningcalculator.shared.components.AppTextField
import com.example.morningcalculator.shared.components.DatePickerField
import com.example.morningcalculator.shared.components.SmallIconButton
import com.example.morningcalculator.shared.components.TimePickerField
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDialog(
    screenTitle: String,
    viewState: RoutineDialogViewState,
    onStateChange: (RoutineDialogViewState) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val resolvedTitle = viewState.title
    val resolvedAnchor = viewState.anchor
    val resolvedDate = viewState.date
    val resolvedTime = viewState.time
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    val options = listOf(
        RoutineScheduleAnchor.START to stringResource(R.string.routine_anchor_start_at),
        RoutineScheduleAnchor.END to stringResource(R.string.routine_anchor_end_at)
    )

    EditorDialogScaffold(
        screenTitle = screenTitle,
        onDismiss = onDismiss,
        headerActions = {
            if (onDelete != null && viewState.routineId != null) {
                SmallIconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.content_desc_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AppTextField(
                    value = resolvedTitle,
                    autofocus = resolvedTitle.isEmpty(),
                    onValueChange = {
                        onStateChange(
                            viewState.copy(title = it)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_name)) }
                )

                Spacer(Modifier.height(12.dp))

                RoutineAnchorSelector(
                    options = options,
                    anchor = resolvedAnchor,
                    onChanged = {
                        onStateChange(
                            viewState.copy(anchor = it)
                        )
                    }
                )

                Spacer(Modifier.height(12.dp))

                DatePickerField(
                    label = stringResource(R.string.label_date),
                    initialDate = resolvedDate,
                    onDateChange = {
                        onStateChange(
                            viewState.copy(date = it)
                        )
                    }
                )

                Spacer(Modifier.height(12.dp))

                TimePickerField(
                    label = if (resolvedAnchor == RoutineScheduleAnchor.START) {
                        stringResource(R.string.routine_start_time)
                    } else {
                        stringResource(R.string.routine_end_time)
                    },
                    initialTime = resolvedTime,
                    onTimeChange = { picked ->
                        val nextTime = LocalTime(picked.hour, picked.minute, 0, 0)
                        onStateChange(
                            viewState.copy(time = nextTime)
                        )
                    }
                )

                Spacer(Modifier.height(12.dp))

                ElevatedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = resolvedTitle.isNotBlank(),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    onClick = {
                        onConfirm()
                        onDismiss()
                    }
                ) { Text(stringResource(R.string.action_ok)) }
            }
        }
    )

    if (showDeleteConfirmation && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.routine_delete_dialog_title)) },
            text = { Text(stringResource(R.string.routine_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}