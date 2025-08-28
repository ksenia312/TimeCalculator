package com.example.morningcalculator.features.home.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.shared.extensions.formatAsDateTime
import com.example.morningcalculator.shared.navigator.Screen
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme

@Composable
fun RoutineListItem(
    links: Routine.Links, navigator: NavHostController, onEdit: (Routine.Links) -> Unit
) {
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
        links = links, onNavigate = onNavigate, onEditClick = {
            isEditing.value = true
        })
}

@Composable
private fun RoutineListItem(
    links: Routine.Links,
    onNavigate: () -> Unit,
    onEditClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .height(IntrinsicSize.Min)
            .heightIn(min = 65.dp)
            .clickable(onClick = onNavigate)
    ) {
        Box(Modifier
            .weight(1f)
            .padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(
                text = "${links.title} (${links.time})", style = MaterialTheme.typography.bodyLarge
            )
        }
        Box(Modifier.fillMaxHeight().padding(4.dp)) {
            IconButton(
                onClick = onEditClick, modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = LocalCustomColorScheme.current.accent
                )
            }
            Text(
                links.modifiedAt.formatAsDateTime(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = LocalCustomColorScheme.current.placeholder
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}