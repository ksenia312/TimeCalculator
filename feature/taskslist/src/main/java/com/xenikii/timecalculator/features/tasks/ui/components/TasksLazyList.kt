package com.xenikii.timecalculator.features.tasks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.shared.extensions.bottomIndent

@Composable
fun TasksLazyList(
    tasks: List<Task>,
    selectedIds: Set<String>,
    onLongPress: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onEditTask: (Task) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        item { Spacer(Modifier.height(16.dp)) }
        tasks.forEach { task ->
            item(key = task.id) {
                val isSelectionMode = selectedIds.isNotEmpty()
                val isSelected = task.id in selectedIds
                TaskListItem(
                    task = task,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onLongPress = { onLongPress(task.id) },
                    onToggleSelect = { onToggleSelect(task.id) },
                    onClick = { onEditTask(task) },
                )
            }
        }
        item { Box(Modifier.bottomIndent()) }
    }
}
