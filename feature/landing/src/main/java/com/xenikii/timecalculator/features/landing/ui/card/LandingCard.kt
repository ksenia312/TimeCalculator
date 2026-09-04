package com.xenikii.timecalculator.features.landing.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.features.landing.presentation.LandingCardTaskViewItem
import com.xenikii.timecalculator.features.landing.presentation.LandingRoutineState
import com.xenikii.timecalculator.shared.animation.routineCardSharedKey
import com.xenikii.timecalculator.shared.extensions.bottomIndent
import com.xenikii.timecalculator.shared.features.RoutineCardStatusRow
import com.xenikii.timecalculator.shared.features.RoutineCardTimeInfo
import com.xenikii.timecalculator.shared.features.RoutineRecurrenceBadge
import com.xenikii.timecalculator.shared.features.routineCard
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme

@Composable
fun LandingCard(
    routineState: LandingRoutineState,
    onNavigate: (routineId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewItem = routineState.cardViewItem
    var expanded by rememberSaveable(routineState.routineId) { mutableStateOf(false) }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .routineCard(
                    viewItem = viewItem,
                    horizontalPadding = PaddingValues(horizontal = 16.dp),
                    sharedKey = routineCardSharedKey(routineState.routineId),
                ) {
                    onNavigate(routineState.routineId)
                },
        ) {
            // --- Header ---
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = viewItem.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 3,
                        color = MaterialTheme.colorScheme.surface,
                    )
                    if (viewItem.recurrence.unit != RoutineRecurrenceUnit.NONE) {
                        Spacer(Modifier.height(4.dp))
                        RoutineRecurrenceBadge(
                            recurrence = viewItem.recurrence,
                            contentColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            textStyle = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    RoutineCardStatusRow(
                        isOngoing = viewItem.isOngoing,
                        isCompleted = viewItem.isCompleted,
                    )
                }
                Spacer(Modifier.width(16.dp))
                RoutineCardTimeInfo(viewItem, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(10.dp))

        LandingCardTasksSection(
            isRoutineOngoing = viewItem.isOngoing,
            isRoutineCompleted = viewItem.isCompleted,
            expanded = expanded && routineState.hasHiddenTasks,
            hasHiddenTasks = routineState.hasHiddenTasks,
            onToggle = { expanded = !expanded },
            completedTasks = routineState.completedTasks,
            previewTasks = routineState.previewTasks,
            futureTasks = routineState.futureTasks,
        )

        Spacer(Modifier.bottomIndent())
    }
}

@Composable
private fun LandingCardTasksSection(
    isRoutineOngoing: Boolean,
    isRoutineCompleted: Boolean,
    expanded: Boolean,
    hasHiddenTasks: Boolean,
    onToggle: () -> Unit,
    completedTasks: List<LandingCardTaskViewItem>,
    previewTasks: List<LandingCardTaskViewItem>,
    futureTasks: List<LandingCardTaskViewItem>,
) {
    val currentTaskBringIntoViewRequester = remember { BringIntoViewRequester() }
    val hasCurrentTask = isRoutineOngoing && previewTasks.isNotEmpty()

    LaunchedEffect(expanded, hasCurrentTask) {
        if (expanded && hasCurrentTask) {
            currentTaskBringIntoViewRequester.bringIntoView()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (expanded) {
            completedTasks.forEach { task ->
                TaskItem(
                    task = task,
                    isCompleted = true,
                    routineIsOngoing = isRoutineOngoing,
                )
            }
        }
        previewTasks.forEachIndexed { index, task ->
            val isCurrentTask = isRoutineOngoing && index == 0
            TaskItem(
                task = task,
                isOngoing = isCurrentTask,
                isCompleted = isRoutineCompleted,
                routineIsOngoing = isRoutineOngoing,
                modifier = if (isCurrentTask) {
                    Modifier.bringIntoViewRequester(currentTaskBringIntoViewRequester)
                } else {
                    Modifier
                },
            )
        }
        if (expanded) {
            futureTasks.forEach { task ->
                TaskItem(
                    task = task,
                    isCompleted = isRoutineCompleted,
                    routineIsOngoing = isRoutineOngoing,
                )
            }
        }
    }

    if (hasHiddenTasks) {
        ExpandToggle(
            expanded = expanded,
            onToggle = onToggle,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun TaskItem(
    task: LandingCardTaskViewItem,
    routineIsOngoing: Boolean,
    isOngoing: Boolean = false,
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    LandingCardTaskItem(
        headerRes = task.headerRes,
        remaining = task.remaining,
        title = task.title,
        start = task.start,
        end = task.end,
        progress = task.progress,
        isOngoing = isOngoing,
        isCompleted = isCompleted,
        routineIsOngoing = routineIsOngoing,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ExpandToggle(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = LocalCustomColorScheme.current.accent

    TextButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Text(
            text = stringResource(
                if (expanded) R.string.landing_hide_all_tasks
                else R.string.landing_show_all_tasks
            ),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
        )
    }
}