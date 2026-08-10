package com.xenikii.timecalculator.features.taskeditor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.TaskRequest
import com.xenikii.timecalculator.features.taskeditor.presentation.CreateTaskViewModel
import com.xenikii.timecalculator.shared.features.AddDurationButton
import com.xenikii.timecalculator.shared.features.DurationInput
import com.xenikii.timecalculator.shared.features.DurationRow
import com.xenikii.timecalculator.shared.features.EditorScreenScaffold
import com.xenikii.timecalculator.shared.features.SaveTaskButton
import com.xenikii.timecalculator.shared.features.TaskNameField
import com.xenikii.timecalculator.shared.features.selectedIndexAfterRemove
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.minutes

@Composable
fun CreateTaskScreen(
    routineId: String? = null,
    viewModel: CreateTaskViewModel = koinViewModel(
        parameters = { parametersOf(routineId) }
    ),
) {
    val navigator = LocalNavigator.current
    val hasRoutine = viewModel.hasRoutine
    var title by remember { mutableStateOf("") }
    val durations = remember { mutableStateListOf(DurationInput()) }
    val showDuplicateError by viewModel.showDuplicateError.collectAsStateWithLifecycle()

    var selectedIndex by remember(hasRoutine) {
        mutableStateOf(if (hasRoutine) 0 else null)
    }

    EditorScreenScaffold(
        screenTitle = stringResource(R.string.task_create_title),
        onDismiss = navigator::navigateBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TaskNameField(
                    title = title,
                    autofocus = true,
                    onValueChange = { title = it },
                )
            }

            itemsIndexed(durations) { index, value ->
                DurationRow(
                    index = index,
                    value = value,
                    selectable = hasRoutine,
                    selected = selectedIndex == index,
                    onSelect = {
                        if (hasRoutine) {
                            selectedIndex = index
                        }
                    },
                    onValueChange = { new -> durations[index] = new },
                    onRemove = {
                        durations.removeAt(index)
                        if (hasRoutine) {
                            selectedIndex = selectedIndexAfterRemove(
                                current = selectedIndex,
                                removedIndex = index,
                                newLastIndex = durations.lastIndex,
                            )
                        }
                    },
                )
            }

            item {
                AddDurationButton(
                    text = stringResource(R.string.task_add_more_durations),
                    onClick = {
                        durations.add(DurationInput())
                        if (hasRoutine) {
                            selectedIndex = durations.lastIndex
                        }
                    },
                )
            }

            item {
                SaveTaskButton(
                    enabled = durations.isNotEmpty() && durations.all(DurationInput::hasAnyValue),
                    onConfirm = {
                        val durationsRes = durations.mapNotNull { it.totalMinutesOrNull()?.minutes }
                        if (durationsRes.size == durations.size) {
                            val saved = viewModel.createTask(
                                TaskRequest(
                                    title = title,
                                    description = "",
                                    durations = durationsRes.filterNotNull()
                                ),
                                selectedDurationIndex = selectedIndex,
                            )
                            if (saved) {
                                navigator.navigateBack()
                            }
                        }
                    },
                )

                if (showDuplicateError) {
                    Text(
                        text = stringResource(R.string.task_duplicate_durations_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}