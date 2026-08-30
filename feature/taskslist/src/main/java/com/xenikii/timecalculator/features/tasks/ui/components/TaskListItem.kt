package com.xenikii.timecalculator.features.tasks.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.shared.components.AppCircleIndicator
import com.xenikii.timecalculator.shared.components.AppListItem
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewConstants
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import com.xenikii.timecalculator.shared.extensions.shortStringValue

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
    AppListItem(
        modifier = Modifier.combinedClickable(
            onClick = { if (isSelectionMode) onToggleSelect() else onClick() },
            onLongClick = { if (!isSelectionMode) onLongPress() else onToggleSelect() },
        ),
        isSelected = isSelected,
        leadingContent = {
            if (isSelectionMode) {

                Icon(
                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )

            } else AppCircleIndicator(
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                foregroundColor = MaterialTheme.colorScheme.primary,
            )
        },
        headlineContent = {
            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(
                        R.string.task_durations,
                        task.data.joinToString { it.duration.shortStringValue() }
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
