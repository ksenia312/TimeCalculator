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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.mapper.copyWithRequest
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.home.ui.components.RoutineDialog
import com.example.morningcalculator.shared.extensions.endAt
import com.example.morningcalculator.shared.extensions.isCompleted
import com.example.morningcalculator.shared.extensions.isOngoing
import com.example.morningcalculator.shared.extensions.stringDateTime
import com.example.morningcalculator.shared.extensions.whenToStart
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import com.example.morningcalculator.shared.navigator.Screen
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import kotlin.time.Instant

@Composable
fun RoutineListItem(
    routine: Routine,
    onEdit: (Routine) -> Unit = {}
) {
    val navigator = LocalNavHostController.current
    val isEditing = remember { mutableStateOf(false) }
    val onNavigate: () -> Unit = {
        navigator.navigate(Screen.Routine.route)
        navigator.currentBackStackEntry?.savedStateHandle?.set(
            "routineId",
            routine.id
        )
    }

    if (isEditing.value) {
        RoutineDialog(
            initialRoutine = routine,
            onConfirm = { request ->
                onEdit(routine.copyWithRequest(request))
                isEditing.value = false
            },
            onDismiss = {
                isEditing.value = false
            },
        )
    }

    RoutineListItem(
        routine = routine,
        onNavigate = onNavigate,
        onEditClick = {
            isEditing.value = true
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

    val (statusPrefix, statusText) = when {
        isCompleted -> "completed on" to routine.endAt().stringDateTime()
        isOngoing -> "running now, ends on" to routine.endAt().stringDateTime()
        else -> "planned for" to routine.whenToStart().stringDateTime()
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
            title = "Morning Routine",
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