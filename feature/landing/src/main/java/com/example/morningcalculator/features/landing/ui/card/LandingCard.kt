package com.example.morningcalculator.features.landing.ui.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.features.landing.presentation.LandingCardTaskViewItem
import com.example.morningcalculator.features.landing.presentation.LandingRoutineState
import com.example.morningcalculator.shared.animation.routineCardSharedKey
import com.example.morningcalculator.shared.features.RoutineCardStatusRow
import com.example.morningcalculator.shared.features.RoutineCardTimeInfo
import com.example.morningcalculator.shared.features.routineCard

private const val EXPAND_ANIMATION_MS = 300

@Composable
fun LandingCard(
    routineState: LandingRoutineState,
    onNavigate: (routineId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewItem = routineState.cardViewItem
    var expanded by rememberSaveable(routineState.routineId) { mutableStateOf(false) }

    val tasks = routineState.taskViewItems
    val currentIndex = routineState.currentTaskIndex

    // Completed tasks, in chronological order (oldest first) so they sit above the current one.
    val completedTasks = if (viewItem.isOngoing && currentIndex != null) {
        tasks.take(currentIndex)
    } else {
        emptyList()
    }
    // For an ongoing routine the "preview" starts at the current task, otherwise from the top.
    val upcomingTasks = if (viewItem.isOngoing && currentIndex != null) {
        tasks.drop(currentIndex)
    } else {
        tasks
    }
    val previewTasks = upcomingTasks.take(2)   // current + next (or first two)
    val futureTasks = upcomingTasks.drop(2)    // revealed on expand
    val hasHiddenTasks = completedTasks.isNotEmpty() || futureTasks.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .routineCard(
                viewItem = viewItem,
                horizontalPadding = 16.dp,
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
                Spacer(Modifier.height(8.dp))
                RoutineCardStatusRow(
                    isOngoing = viewItem.isOngoing,
                    isCompleted = viewItem.isCompleted,
                )
            }
            Spacer(Modifier.width(16.dp))
            RoutineCardTimeInfo(viewItem, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(32.dp))

        LandingCardBody(
            isRoutineOngoing = viewItem.isOngoing,
            isRoutineCompleted = viewItem.isCompleted,
            expanded = expanded && hasHiddenTasks,
            hasHiddenTasks = hasHiddenTasks,
            onToggle = { expanded = !expanded },
            completedTasks = completedTasks,
            previewTasks = previewTasks,
            futureTasks = futureTasks,
        )
    }
}

@Composable
private fun ColumnScope.LandingCardBody(
    isRoutineOngoing: Boolean,
    isRoutineCompleted: Boolean,
    expanded: Boolean,
    hasHiddenTasks: Boolean,
    onToggle: () -> Unit,
    completedTasks: List<LandingCardTaskViewItem>,
    previewTasks: List<LandingCardTaskViewItem>,
    futureTasks: List<LandingCardTaskViewItem>,
) {
    val tasksModifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())

    Column(
        modifier = tasksModifier,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (expanded) {
                completedTasks.forEach { task ->
                    TaskItem(
                        task = task,
                        isCompleted = true
                    )
                }
            }
            previewTasks.forEachIndexed { index, task ->
                TaskItem(
                    task = task,
                    isOngoing = isRoutineOngoing && index == 0,
                    isCompleted = isRoutineCompleted
                )
            }
            if (expanded) {
                futureTasks.forEach { task ->
                    TaskItem(
                        task = task,
                        isCompleted = isRoutineCompleted
                    )
                }
            }
        }
    }

    if (hasHiddenTasks) {
        ExpandToggle(expanded = expanded, onToggle = onToggle)
    }
}

@Composable
private fun TaskItem(
    task: LandingCardTaskViewItem,
    isOngoing: Boolean = false,
    isCompleted: Boolean = false,
) {
    LandingCardTaskItem(
        headerRes = task.headerRes,
        remaining = task.remaining,
        title = task.title,
        start = task.start,
        end = task.end,
        progress = task.progress,
        isOngoing = isOngoing,
        modifier = Modifier.fillMaxWidth(),
        isCompleted = isCompleted
    )
}

@Composable
private fun ExpandToggle(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(EXPAND_ANIMATION_MS),
        label = "chevronRotation",
    )
    val contentColor = MaterialTheme.colorScheme.surface

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
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.rotate(rotation),
        )
    }
}