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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.home.ui.components.applyRoutineDialogViewState
import com.example.morningcalculator.features.home.ui.components.RoutineDialog
import com.example.morningcalculator.features.home.ui.components.RoutineDialogViewState
import com.example.morningcalculator.features.home.ui.components.toRoutineDialogViewState
import com.example.morningcalculator.shared.extensions.endAt
import com.example.morningcalculator.shared.extensions.isCompleted
import com.example.morningcalculator.shared.extensions.isOngoing
import com.example.morningcalculator.shared.extensions.stringDateTime
import com.example.morningcalculator.shared.extensions.whenToStart
import com.example.morningcalculator.shared.navigator.AppRoute
import com.example.morningcalculator.shared.navigator.LocalNavigator
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import kotlin.time.Instant

@Composable
fun RoutineListItem(
    routine: Routine,
    onEdit: (Routine) -> Unit = {}
) {
    val navigator = LocalNavigator.current
    val editingRoutineDialogState = remember { mutableStateOf<RoutineDialogViewState?>(null) }
    val onNavigate: () -> Unit = {
        navigator.navigateTo(
            AppRoute.Routine(routineId = routine.id),
        )
    }

    if (editingRoutineDialogState.value != null) {
        val routineDialogViewState = editingRoutineDialogState.value!!
        RoutineDialog(
            screenTitle = stringResource(R.string.routine_dialog_edit_title),
            viewState = routineDialogViewState,
            onStateChange = { editingRoutineDialogState.value = it },
            onConfirm = {
                onEdit(routine.applyRoutineDialogViewState(routineDialogViewState))
                editingRoutineDialogState.value = null
            },
            onDismiss = {
                editingRoutineDialogState.value = null
            },
        )
    }

    RoutineListItem(
        routine = routine,
        onNavigate = onNavigate,
        onEditClick = {
            editingRoutineDialogState.value = routine.toRoutineDialogViewState()
        }
    )
}

@Composable
private fun RoutineListItem(
    routine: Routine,
    onNavigate: () -> Unit,
    onEditClick: () -> Unit,
) {
    val isCompleted = routine.isCompleted()
    val isOngoing = routine.isOngoing()
    val context = LocalContext.current

    val (statusPrefix, statusText) = when {
        isCompleted -> stringResource(R.string.routines_list_status_completed_on) to
            routine.endAt().stringDateTime(context = context)
        isOngoing -> stringResource(R.string.routines_list_status_running_ends_on) to
            routine.endAt().stringDateTime(context = context)
        else -> stringResource(R.string.routines_list_status_planned_for) to
            routine.whenToStart().stringDateTime(context = context)
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
        RoutineListItem(
            routine = routine,
            onNavigate = {},
            onEditClick = {}
        )
    }
}