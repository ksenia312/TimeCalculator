package com.example.morningcalculator.features.routineslist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.home.ui.components.RoutineDialog
import com.example.morningcalculator.shared.extensions.formatAsDateTime
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import com.example.morningcalculator.shared.navigator.Screen
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import kotlinx.datetime.LocalTime

@Composable
fun RoutineListItem(
    links: Routine.Links,
    onEdit: (Routine.Links) -> Unit = {}
) {
    val navigator = LocalNavHostController.current
    val isEditing = remember { mutableStateOf(false) }
    val onNavigate: () -> Unit = {
        navigator.navigate(Screen.Routine.route)
        navigator.currentBackStackEntry?.savedStateHandle?.set(
            "routineId", links.id
        )
    }
    if (isEditing.value) {
        RoutineDialog(
            initialRoutine = links,
            onConfirm = { request ->
                onEdit(links.copy(title = request.title, time = request.time))
            },
            onDismiss = {
                isEditing.value = false
            },
        )
    }

    RoutineListItem(
        links = links,
        onNavigate = onNavigate,
        onEditClick = {
            isEditing.value = true
        }
    )
}

@Composable
private fun RoutineListItem(
    links: Routine.Links,
    onNavigate: () -> Unit,
    onEditClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onNavigate),
        headlineContent = {
            Column {
                Text(
                    text = "${links.title} (${links.modifiedAt.formatAsDateTime()})",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Starts at ${links.time}",
                    style = MaterialTheme.typography.bodySmall
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
                    tint = LocalCustomColorScheme.current.accent
                )
            }
        },
    )
}


@PreviewAll
@Composable
fun RoutineListItemPreview() {
    PreviewTheme {
        val routine = Routine.Links(
            id = "1",
            title = "Morning Routine",
            color = "0xFFE57373",
            time = LocalTime(7, 0),
            modifiedAt = System.currentTimeMillis(),
            links = listOf()
        )
        RoutineListItem(
            links = routine,
            onNavigate = {},
            onEditClick = {}
        )
    }
}