package com.xenikii.timecalculator.features.routine.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.extensions.dragHandle

/**
 * Trailing content of a routine task row while in edit mode: dragging this handle reorders
 * the task within the routine, replacing the duration dropdown shown outside edit mode.
 */
@Composable
fun RoutineTaskDragHandle(
    index: Int,
    itemCount: Int,
    draggingIndex: MutableState<Int?>,
    dragOffsetY: MutableState<Float>,
    onMove: (from: Int, to: Int) -> Unit,
    onDrop: () -> Unit,
    onCancel: () -> Unit,
) {
    Icon(
        imageVector = Icons.Filled.DragHandle,
        contentDescription = stringResource(R.string.content_desc_reorder_task),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(8.dp)
            .dragHandle(
                index = index,
                draggingIndex = draggingIndex,
                dragOffsetY = dragOffsetY,
                itemCount = itemCount,
                onMove = onMove,
                onDrop = onDrop,
                onCancel = onCancel,
            ),
    )
}
