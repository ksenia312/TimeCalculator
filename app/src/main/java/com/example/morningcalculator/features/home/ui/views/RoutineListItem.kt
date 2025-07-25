package com.example.morningcalculator.features.home.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.shared.navigator.Screen

@Composable
fun RoutineListItem(routine: Routine, navigator: NavHostController, onEdit: (Routine) -> Unit) {
    val isEditing = remember { mutableStateOf(false) }
    val onNavigate: () -> Unit = {
        navigator.navigate(Screen.Routine.route)
        navigator.currentBackStackEntry?.savedStateHandle?.set(
            "routineId", routine.id
        )
    }
    if (isEditing.value) {
        RoutineDialog(
            initialRoutine = routine,
            onConfirm = { request ->
                onEdit(routine.copy(title = request.title, time = request.time))
            },
            onDismiss = {
                isEditing.value = false
            },
        )
    }
    ListItem(
        headlineContent = { Text(text = "${routine.title} (${routine.time})") },
        trailingContent = {
            Row {
                IconButton(onClick = {
                    isEditing.value = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null
                    )
                }
                IconButton(onClick = onNavigate) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                        contentDescription = null
                    )
                }


            }
        },
        modifier = Modifier.clickable(
            onClick = onNavigate
        )
    )
}
