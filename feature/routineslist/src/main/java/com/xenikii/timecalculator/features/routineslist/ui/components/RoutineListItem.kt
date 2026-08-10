package com.xenikii.timecalculator.features.routineslist.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.features.routineslist.presentation.RoutineListItemState
import com.xenikii.timecalculator.shared.extensions.stringDateTime
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import com.xenikii.timecalculator.shared.components.AppListItem
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme
import kotlin.time.Instant

@Composable
fun RoutineListItem(
    item: RoutineListItemState,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {},
    onToggleSelect: () -> Unit = {},
) {
    val navigator = LocalNavigator.current

    RoutineListItem(
        item = item,
        isSelectionMode = isSelectionMode,
        isSelected = isSelected,
        onLongPress = onLongPress,
        onToggleSelect = onToggleSelect,
        onNavigate = {
            navigator.navigateTo(AppRoute.Routine(routineId = item.routine.id))
        },
        onEditClick = {
            navigator.navigateTo(
                AppRoute.EditRoutine(
                    routineId = item.routine.id,
                    fromRoutineScreen = false,
                )
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoutineListItem(
    item: RoutineListItemState,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onLongPress: () -> Unit,
    onToggleSelect: () -> Unit,
    onNavigate: () -> Unit,
    onEditClick: () -> Unit,
) {
    val context = LocalContext.current
    val routine = item.routine
    val isCompleted = item.cardViewItem.isCompleted
    val isOngoing = item.cardViewItem.isOngoing

    val (statusPrefix, statusText) = when {
        isCompleted -> stringResource(R.string.routines_list_status_completed_on) to
                item.cardViewItem.endInstant.stringDateTime(context = context)

        isOngoing -> stringResource(R.string.routines_list_status_running_ends_on) to
                item.cardViewItem.endInstant.stringDateTime(context = context)

        else -> stringResource(R.string.routines_list_status_planned_for) to
                item.cardViewItem.startInstant.stringDateTime(context = context)
    }

    val color = when {
        isCompleted -> LocalCustomColorScheme.current.placeholder
        isOngoing -> LocalCustomColorScheme.current.accent
        else -> LocalCustomColorScheme.current.label
    }

    AppListItem(
        modifier = Modifier.combinedClickable(
            onClick = {
                if (isSelectionMode) onToggleSelect() else onNavigate()
            },
            onLongClick = {
                if (!isSelectionMode) onLongPress() else onToggleSelect()
            },
        ),
        isSelected = isSelected,
        leadingContent = {
            Box(
                Modifier.size(24.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                if (isSelectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Box(
                        Modifier
                            .size(14.dp)
                            .background(color, shape = CircleShape)
                    )
                }
            }
        },
        headlineContent = {
            Column {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3
                )
                Text(
                    text = buildAnnotatedString {
                        append(statusPrefix)
                        append(" ")
                        pushStyle(
                            style = SpanStyle(fontWeight = FontWeight.Bold)
                        )
                        append(statusText)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
        },
        trailingContent = {
            if (!isSelectionMode) {
                IconButton(
                    onClick = onEditClick,
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditCalendar,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}

@PreviewAll
@Composable
fun RoutineListItemPreview() {
    PreviewTheme {
        val routine = Routine(
            id = "1",
            title = stringResource(R.string.sample_time_routine),
            color = "0xFFE57373",
            scheduledAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            modifiedAt = System.currentTimeMillis(),
            data = listOf()
        )
        val item = com.xenikii.timecalculator.features.routineslist.presentation.RoutineListItemState(
            routine = routine,
            schedule = com.xenikii.timecalculator.domain.model.RoutineSchedule(
                routineId = routine.id,
                routineTitle = routine.title,
                effectiveStart = routine.scheduledAt,
                end = routine.scheduledAt,
                totalDuration = kotlin.time.Duration.ZERO,
                tasks = emptyList(),
                signature = "",
            ),
            cardViewItem = com.xenikii.timecalculator.shared.viewitem.RoutineCardViewItem(
                isOngoing = false,
                isCompleted = false,
                startLabelRes = R.string.routine_card_will_start,
                endLabelRes = R.string.routine_card_will_end,
                startInstant = routine.scheduledAt,
                endInstant = routine.scheduledAt,
                title = routine.title,
                willStartIn = kotlin.time.Duration.ZERO,
            ),
        )
        RoutineListItem(
            item = item,
            isSelectionMode = false,
            isSelected = false,
            onLongPress = {},
            onToggleSelect = {},
            onNavigate = {},
            onEditClick = {}
        )
    }
}