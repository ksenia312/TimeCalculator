package com.example.morningcalculator.features.routine.ui.views.tasks_bottom_sheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineFullLink
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksBottomSheet(
    onDismiss: () -> Unit,
    onShowAddTasksDialog: () -> Unit,
    viewModel: RoutineViewModel,
    routine: Routine.Full
) {
    val tasks by viewModel.tasks.collectAsState()
    val links = remember { routine.data.toMutableStateList() }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    LaunchedEffect(links) {
        viewModel.editLinksInRoutine(links)
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        modifier = Modifier.padding(top = 32.dp)
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(12.dp, 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Manage tasks", style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                tasks.forEachIndexed { index, task ->
                    item(key = task.id) {
                        TasksBottomSheetItem(
                            links, task
                        )
                    }
                    if (index < tasks.lastIndex) {
                        item {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                    }
                }
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(), onClick = {
                    onShowAddTasksDialog()
                    onDismiss()
                }) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Image(
                        painterResource(R.drawable.add_circle),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                    Text("Create a new task")
                }
            }
        }
    }
}


fun MutableList<RoutineFullLink>.addTask(task: Task) {
    add(
        RoutineFullLink(
            id = UUID.randomUUID().toString(),
            task = task,
            subData = task.dataSortedByDuration.firstOrNull() ?: SubData.tenMins
        )
    )
}
