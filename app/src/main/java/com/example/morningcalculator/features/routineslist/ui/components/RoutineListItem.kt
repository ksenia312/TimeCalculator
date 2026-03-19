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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.home.ui.components.RoutineDialog
import com.example.morningcalculator.shared.extensions.formatAsDateTime
import com.example.morningcalculator.shared.extensions.whenToStart
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import com.example.morningcalculator.shared.navigator.Screen
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant

@Composable
fun RoutineListItem(
    links: Routine,
    onEdit: (Routine) -> Unit = {}
) {
    val navigator = LocalNavHostController.current
    val isEditing = remember { mutableStateOf(false) }
    val onNavigate: () -> Unit = {
        navigator.navigate(Screen.Routine.route)
        navigator.currentBackStackEntry?.savedStateHandle?.set(
            "routineId",
            links.id
        )
    }

    if (isEditing.value) {
        RoutineDialog(
            initialRoutine = links,
            onConfirm = { request ->
                onEdit(
                    links.copy(
                        title = request.title,
                        scheduledAt = request.scheduledAt
                    )
                )
                isEditing.value = false
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
    links: Routine,
    onNavigate: () -> Unit,
    onEditClick: () -> Unit,
) {
    val startText = links.whenToStart()
        .toJavaLocalDateTime()
        .format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.ENGLISH))

    ListItem(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onNavigate),
        headlineContent = {
            Column {
                val text = buildAnnotatedString {
                    append("${links.title} (")
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = LocalCustomColorScheme.current.accent
                        )
                    )
                    append(links.modifiedAt.formatAsDateTime())
                    pop()
                    append(")")
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "planned for $startText",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalCustomColorScheme.current.accent
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
            links = routine,
            onNavigate = {},
            onEditClick = {}
        )
    }
}