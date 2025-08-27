package com.example.morningcalculator.features.routine.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineFullLink
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksBottomSheet(
    onDismiss: () -> Unit, viewModel: RoutineViewModel, routine: Routine.Full
) {
    val tasks by viewModel.tasks.collectAsState()
    val links = remember { routine.data.toMutableStateList() }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        modifier = Modifier.padding(top = 32.dp)
    ) {
        Column(
            Modifier.fillMaxHeight()
        ) {
            Text("Modify tasks")
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.weight(1f)) {
                tasks.forEach { task ->
                    item(key = task.id) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(12.dp, 4.dp)
                                .clickable(
                                    onClick = {
                                        links.addTask(task)
                                    }),
                        ) {
                            HeadlineContent(modifier = Modifier.weight(1f), links, task)
                            TrailingIcons(links, task)
                        }
                    }
                }
            }
            ElevatedButton(onClick = {
                viewModel.editLinksInRoutine(links)
                onDismiss()
            }) {
                Text("Submit")
            }
        }
    }
}

@Composable
private fun HeadlineContent(
    modifier: Modifier, links: MutableList<RoutineFullLink>, task: Task
) {
    val hasTask = links.any { it.task.id == task.id }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (hasTask) LocalCustomColorScheme.current.accent
                    else LocalCustomColorScheme.current.accentLight,
                    shape = RoundedCornerShape(100.dp)
                )
                .size(40.dp), contentAlignment = Alignment.Center
        ) {
            if (hasTask) {
                Text(
                    links.filter { it.task.id == task.id }.size.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary, lineHeight = 1.sp
                    ),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    Icons.Default.Done,
                    contentDescription = "",
                    tint = LocalCustomColorScheme.current.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(text = task.title)
    }
}

@Composable
private fun TrailingIcons(links: MutableList<RoutineFullLink>, task: Task) {
    val hasTask = links.any { it.task.id == task.id }
    Box(Modifier.heightIn(min = 50.dp)) {
        AnimatedVisibility(visible = hasTask) {
            Row {
                IconButton(onClick = {
                    val el = links.firstOrNull { it.task.id == task.id }
                    if (el != null) links.remove(el)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.remove_circle),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(LocalCustomColorScheme.current.label)
                    )
                }
                IconButton(onClick = {
                    links.addTask(task)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.add_circle),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(LocalCustomColorScheme.current.label)
                    )
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
            subData = task.data.firstOrNull() ?: SubData.tenMins
        )
    )
}