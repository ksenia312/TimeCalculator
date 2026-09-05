package com.xenikii.timecalculator.features.routine.ui.components.tasksselection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.shared.components.AppButtonMedium
import com.xenikii.timecalculator.shared.components.AppListItem
import com.xenikii.timecalculator.shared.components.BackButton
import com.xenikii.timecalculator.shared.extensions.stringDateTime

@Composable
fun BottomSheetTitleContainer(content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.heightIn(min = 48.dp)
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyFromRoutineBottomSheet(
    routines: List<Routine>,
    onDismiss: () -> Unit,
    onConfirm: (links: List<RoutineLink>) -> Unit,
) {
    var selectedRoutine by remember { mutableStateOf<Routine?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        modifier = Modifier.padding(top = 32.dp),
        properties = ModalBottomSheetProperties(
            securePolicy = SecureFlagPolicy.SecureOn,
            shouldDismissOnBackPress = true,
        )
    ) {
        val routine = selectedRoutine
        if (routine == null) {
            RoutinePickerContent(
                routines = routines,
                onRoutineSelected = { selectedRoutine = it },
            )
        } else {
            RoutineTasksContent(
                sourceRoutine = routine,
                onBack = { selectedRoutine = null },
                onConfirm = { links ->
                    onConfirm(links)
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun RoutinePickerContent(
    routines: List<Routine>,
    onRoutineSelected: (Routine) -> Unit,
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxHeight()
            .padding(12.dp, 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BottomSheetTitleContainer {
            Text(
                stringResource(R.string.copy_routine_pick_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        if (routines.isEmpty()) {
            Text(
                stringResource(R.string.copy_routine_pick_empty),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(routines, key = { it.id }) { routine ->
                    AppListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRoutineSelected(routine) },
                        headlineContent = {
                            Text(routine.title, style = MaterialTheme.typography.titleMedium)
                        },
                        supportingContent = {
                            Text(
                                routine.scheduledAt.stringDateTime(context = context),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineTasksContent(
    sourceRoutine: Routine,
    onBack: () -> Unit,
    onConfirm: (links: List<RoutineLink>) -> Unit,
) {
    val candidateLinks = sourceRoutine.data
    val candidateTasks = remember(candidateLinks) {
        candidateLinks.map { it.task }.distinctBy { it.id }
    }
    val selectedLinks = remember(candidateLinks) { candidateLinks.toMutableStateList() }

    Column(
        Modifier
            .fillMaxHeight()
            .padding(12.dp, 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BottomSheetTitleContainer {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackButton(overrideOnBack = onBack)
                Text(
                    stringResource(R.string.copy_routine_tasks_title, sourceRoutine.title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (candidateTasks.isEmpty()) {
            Text(
                stringResource(R.string.copy_routine_tasks_empty),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                candidateTasks.forEachIndexed { index, task ->
                    item(key = task.id) {
                        TasksBottomSheetItem(selectedLinks, task)
                    }
                    if (index < candidateTasks.lastIndex) {
                        item {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                    }
                }
            }
            AppButtonMedium(
                onClick = { onConfirm(selectedLinks.toList()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        R.string.copy_routine_tasks_confirm,
                        selectedLinks.size
                    )
                )
            }
        }
    }
}
