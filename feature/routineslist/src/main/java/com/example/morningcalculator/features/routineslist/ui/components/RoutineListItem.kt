package com.example.morningcalculator.features.routineslist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.features.routineslist.presentation.RoutineListItemState
import com.example.morningcalculator.shared.navigator.AppRoute
import com.example.morningcalculator.shared.navigator.LocalNavigator
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import com.example.morningcalculator.shared.extensions.stringDateTime
import kotlin.time.Instant

@Composable
fun RoutineListItem(
    item: RoutineListItemState,
) {
    val navigator = LocalNavigator.current
    val onNavigate: () -> Unit = {
        navigator.navigateTo(
            AppRoute.Routine(routineId = item.routine.id),
        )
    }

    RoutineListItem(
        item = item,
        onNavigate = onNavigate,
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

@Composable
private fun RoutineListItem(
    item: RoutineListItemState,
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

    ListItem(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onNavigate),
        leadingContent = {
            Box(
                Modifier
                    .size(14.dp)
                    .background(color, shape = CircleShape)
            )
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
            IconButton(
                onClick = onEditClick,
            ) {
                Icon(
                    imageVector = Icons.Filled.EditCalendar,
                    contentDescription = null,
                )
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
            title = stringResource(R.string.sample_morning_routine),
            color = "0xFFE57373",
            scheduledAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            modifiedAt = System.currentTimeMillis(),
            data = listOf()
        )
        val item = com.example.morningcalculator.features.routineslist.presentation.RoutineListItemState(
            routine = routine,
            schedule = com.example.morningcalculator.domain.model.RoutineSchedule(
                routineId = routine.id,
                routineTitle = routine.title,
                effectiveStart = routine.scheduledAt,
                end = routine.scheduledAt,
                totalDuration = kotlin.time.Duration.ZERO,
                tasks = emptyList(),
                signature = "",
            ),
            cardViewItem = com.example.morningcalculator.shared.viewitem.RoutineCardViewItem(
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
            onNavigate = {},
            onEditClick = {}
        )
    }
}