package com.example.morningcalculator.features.tasks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewConstants
import com.example.morningcalculator.shared.preview.PreviewTheme

@Composable
fun TaskListItem(
    task: Task
) {
    ListItem(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface),
        headlineContent = {
            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Durations: ${task.data.joinToString { it.duration.toString() }}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

        },
    )
}


@PreviewAll
@Composable
fun RoutineListItemPreview() {
    PreviewTheme {
        TaskListItem(
            PreviewConstants.tasks.first()
        )
    }
}