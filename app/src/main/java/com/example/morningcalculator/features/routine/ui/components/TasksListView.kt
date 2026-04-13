package com.example.morningcalculator.features.routine.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.features.routine.presentation.RoutineViewModel
import com.example.morningcalculator.features.routine.ui.components.taskscreen.EditTaskScreen
import com.example.morningcalculator.shared.extensions.stringTime
import com.example.morningcalculator.shared.extensions.whenToStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListView(
    routine: Routine, viewModel: RoutineViewModel
) {
    val whenToGetUp = routine.whenToStart().stringTime()
    val fullLinks = remember(routine) { routine.data.toMutableStateList() }
    val draggingIndex = remember { mutableStateOf<Int?>(null) }
    val dragOffsetY = remember { mutableFloatStateOf(0f) }
    val editingLink = remember { mutableStateOf<RoutineLink?>(null) }

    if (editingLink.value != null) {
        val link = editingLink.value!!
        EditTaskScreen(
            initialTask = link.task,
            initialSubDataId = link.subData?.id,
            onDismiss = { editingLink.value = null },
            onDelete = { viewModel.deleteTask(link.id) },
            deleteIcon = {
                Image(
                    painterResource(R.drawable.unlink),
                    contentDescription = "delete",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
                )
            },
            onConfirm = { request, selectedIndex ->
                viewModel.editTask(
                    request, selectedIndex ?: 0, linkId = link.id
                )
            },
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item { Spacer(Modifier.height(12.dp)) }
        if (fullLinks.isNotEmpty()) item(key = "wakeUp") {
            TimeSegment(whenToGetUp.toString(), isTitle = true, useSeparator = false)
        }
        itemsIndexed(
            items = fullLinks, key = { _, link -> link.id }) { index, link ->
            RoutineTaskItem(
                routineLinks = fullLinks,
                index = index,
                linkFull = link,
                routine = routine,
                viewModel = viewModel,
                draggingIndex = draggingIndex,
                dragOffsetY = dragOffsetY,
                editingLink = editingLink,
            )
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}
