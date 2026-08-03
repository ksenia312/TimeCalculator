package com.xenikii.timecalculator.features.tasks.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewConstants
import com.xenikii.timecalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskListItem(
    task: Task,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {},
    onToggleSelect: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    ListItem(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleSelect() else onClick() },
                onLongClick = { if (!isSelectionMode) onLongPress() else onToggleSelect() },
            ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = if (isSelectionMode) {
            {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
        } else null,
        headlineContent = {
            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(
                        R.string.task_durations,
                        task.data.joinToString { it.duration.toString() }
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
    )
}

@PreviewAll
@Composable
fun TaskListItemPreview() {
    PreviewTheme {
        TaskListItem(
            PreviewConstants.tasks.first()
        )
    }
}
