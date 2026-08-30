package com.xenikii.timecalculator.features.routineeditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.domain.model.RoutineScheduleAnchor
import com.xenikii.timecalculator.features.routineeditor.presentation.CreateRoutineViewModel
import com.xenikii.timecalculator.features.routineeditor.presentation.EditRoutineViewModel
import com.xenikii.timecalculator.features.routineeditor.presentation.EditRoutineViewState
import com.xenikii.timecalculator.features.routineeditor.ui.components.RoutineAnchorSelector
import com.xenikii.timecalculator.features.routineeditor.ui.components.RoutineDaysOfWeekSelector
import com.xenikii.timecalculator.features.routineeditor.ui.components.RoutineRecurrenceUnitSelector
import com.xenikii.timecalculator.shared.components.AppButtonMedium
import com.xenikii.timecalculator.shared.components.AppTextField
import com.xenikii.timecalculator.shared.components.DatePickerField
import com.xenikii.timecalculator.shared.components.DeleteConfirmationDialog
import com.xenikii.timecalculator.shared.components.SmallIconButton
import com.xenikii.timecalculator.shared.components.TimePickerField
import com.xenikii.timecalculator.shared.features.EditorScreenScaffold
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import kotlinx.datetime.LocalTime
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CreateRoutineScreen(
    viewModel: CreateRoutineViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current
    val form by viewModel.viewState.collectAsStateWithLifecycle()
    RoutineEditorScreen(
        screenTitle = stringResource(R.string.routine_dialog_add_title),
        viewState = form,
        onStateChange = viewModel::onStateChange,
        onConfirm = {
            viewModel.saveRoutine()
            navigator.navigateBack()
        },
        onDismiss = navigator::navigateBack,
    )
}

@Composable
fun EditRoutineScreen(
    routineId: String,
    viewModel: EditRoutineViewModel = koinViewModel(
        parameters = { parametersOf(routineId) }
    ),
) {
    val navigator = LocalNavigator.current
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    when (val viewState = state) {
        EditRoutineViewState.Loading -> CircularProgressIndicator()
        EditRoutineViewState.Error -> {
            EditorScreenScaffold(
                screenTitle = stringResource(R.string.routine_dialog_edit_title),
                onDismiss = navigator::navigateBack,
            ) { padding ->
                Text(
                    text = stringResource(R.string.top_bar_error),
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp),
                )
            }
        }

        is EditRoutineViewState.Success -> {
            RoutineEditorScreen(
                screenTitle = stringResource(R.string.routine_dialog_edit_title),
                viewState = viewState.form,
                onStateChange = viewModel::onStateChange,
                onConfirm = {
                    viewModel.saveRoutine()
                    navigator.navigateBack()
                },
                onDismiss = navigator::navigateBack,
                onDelete = {
                    viewModel.deleteRoutine()
                    navigator.navigateTo(AppRoute.Home)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineEditorScreen(
    screenTitle: String,
    viewState: RoutineEditorFormState,
    onStateChange: (RoutineEditorFormState) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val resolvedTitle = viewState.title
    val resolvedAnchor = viewState.anchor
    val resolvedRecurrenceUnit = viewState.recurrenceUnit
    val resolvedRecurrenceInterval = viewState.recurrenceInterval
    val resolvedDate = viewState.date
    val resolvedTime = viewState.time
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    val options = listOf(
        RoutineScheduleAnchor.START to stringResource(R.string.routine_anchor_start_at),
        RoutineScheduleAnchor.END to stringResource(R.string.routine_anchor_end_at)
    )
    val recurrenceUnitOptions = listOf(
        RoutineRecurrenceUnit.DAY to stringResource(R.string.routine_recurrence_unit_day),
        RoutineRecurrenceUnit.WEEK to stringResource(R.string.routine_recurrence_unit_week),
        RoutineRecurrenceUnit.MONTH to stringResource(R.string.routine_recurrence_unit_month),
        RoutineRecurrenceUnit.YEAR to stringResource(R.string.routine_recurrence_unit_year),
    )
    val repeatsEnabled = resolvedRecurrenceUnit != RoutineRecurrenceUnit.NONE

    EditorScreenScaffold(
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
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))
                AppTextField(
                    value = resolvedTitle,
                    autofocus = resolvedTitle.isEmpty(),
                    onValueChange = {
                        onStateChange(
                            viewState.copy(title = it)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_name)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
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

                val onCheckerChange: (Boolean) -> Unit = { enabled ->
                    onStateChange(
                        viewState.copy(
                            recurrenceUnit = if (enabled) {
                                if (resolvedRecurrenceUnit == RoutineRecurrenceUnit.NONE) {
                                    RoutineRecurrenceUnit.DAY
                                } else {
                                    resolvedRecurrenceUnit
                                }
                            } else {
                                RoutineRecurrenceUnit.NONE
                            },
                        )
                    )
                }

                DatePickerField(
                    label = if (repeatsEnabled) {
                        stringResource(R.string.label_repeat_start_date)
                    } else {
                        stringResource(R.string.label_date)
                    },
                    initialDate = resolvedDate,
                    onDateChange = {
                        onStateChange(
                            viewState.withDate(it)
                        )
                    }
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.extraLarge
                        )
                        .clickable {
                            onCheckerChange(!repeatsEnabled)
                        }
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.routine_repeat_label),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = repeatsEnabled,
                        onCheckedChange = { enabled ->
                            onCheckerChange(enabled)
                        },
                    )
                }

                if (repeatsEnabled) {
                    Spacer(Modifier.height(12.dp))

                    AppTextField(
                        value = resolvedRecurrenceInterval.toString(),
                        onValueChange = { value ->
                            val parsed = value.toIntOrNull()?.coerceAtLeast(1)
                            onStateChange(
                                viewState.copy(
                                    recurrenceInterval = parsed ?: 1,
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(stringResource(R.string.routine_repeat_every_label)) },
                    )

                    Spacer(Modifier.height(12.dp))

                    RoutineRecurrenceUnitSelector(
                        options = recurrenceUnitOptions,
                        unit = resolvedRecurrenceUnit,
                        onChanged = {
                            onStateChange(
                                viewState.withRecurrenceUnit(it)
                            )
                        },
                    )

                    if (resolvedRecurrenceUnit == RoutineRecurrenceUnit.WEEK) {
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.routine_repeat_on_days_label),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        RoutineDaysOfWeekSelector(
                            selectedDays = viewState.effectiveRecurrenceDaysOfWeek(),
                            onToggle = { day ->
                                val updated = viewState.effectiveRecurrenceDaysOfWeek()
                                    .toMutableSet()
                                    .apply { if (!add(day)) remove(day) }
                                onStateChange(
                                    viewState.copy(recurrenceDaysOfWeek = updated)
                                )
                            },
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                AppButtonMedium(
                    enabled = resolvedTitle.isNotBlank(),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    onClick = {
                        onConfirm()
                    }
                ) { Text(stringResource(R.string.action_save)) }

                Spacer(Modifier.height(16.dp))
            }
        }
    )

    if (showDeleteConfirmation && onDelete != null) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.routine_delete_dialog_title),
            message = stringResource(R.string.routine_delete_dialog_message),
            onConfirm = onDelete,
            onDismiss = { showDeleteConfirmation = false },
        )
    }
}
