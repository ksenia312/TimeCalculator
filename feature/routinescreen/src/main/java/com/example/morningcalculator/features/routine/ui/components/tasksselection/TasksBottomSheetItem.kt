package com.example.morningcalculator.features.routine.ui.components.tasksselection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.Task

@Composable
fun TasksBottomSheetItem(
    links: MutableList<RoutineLink>,
    task: Task
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .heightIn(min = 60.dp)
            .clickable { links.addTask(task) }) {
        TasksBottomSheetItemHeading(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp, 4.dp), links, task
        )
        TasksBottomSheetItemTrailing(links, task)
    }
}