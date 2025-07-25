package com.example.morningcalculator.features.routine.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksBottomSheet(
    onDismiss: () -> Unit,
    viewModel: RoutineViewModel,
) {
    val notIncludedTasks by viewModel.notIncludedTasks.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var openedTaskId by remember { mutableStateOf<String?>(null) }
    ModalBottomSheet(
        onDismissRequest = { onDismiss() }, sheetState = sheetState,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Select task")
            Spacer(Modifier.height(12.dp))
            LazyColumn {
                notIncludedTasks.forEach { task ->
                    item(key = task.id) {
                        Column {
                            ListItem(
                                headlineContent = { Text(text = task.title) },
                                modifier = Modifier.clickable(
                                    onClick = {
                                        openedTaskId = task.id
                                    })
                            )
                            if (openedTaskId == task.id) {
                                Column {
                                    task.data.forEach { subData ->
                                        ListItem(
                                            colors = ListItemDefaults.colors(
                                                containerColor = Color.LightGray.copy(alpha = 0.3f)
                                            ),
                                            headlineContent = { Text(text = subData.duration.inWholeMinutes.toString() + " min") },
                                            modifier = Modifier.clickable(
                                                onClick = {
                                                    scope.launch {
                                                        viewModel.addOrEditTaskInRoutine(task, subData)
                                                        sheetState.hide()
                                                        onDismiss()
                                                    }
                                                })
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}