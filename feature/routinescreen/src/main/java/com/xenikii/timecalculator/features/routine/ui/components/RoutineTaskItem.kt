package com.xenikii.timecalculator.features.routine.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.features.routine.presentation.RoutineViewModel
import com.xenikii.timecalculator.shared.extensions.dragOffset
import com.xenikii.timecalculator.shared.extensions.stringTime
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme

/**
 * Fixed height for the row's trailing slot (duration dropdown / drag handle), so switching
 * between them on entering or leaving edit mode doesn't change the row's overall height.
 */
private val TrailingContentHeight = 56.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoutineTaskItem(
    link: RoutineLink,
    routine: Routine,
    index: Int,
    itemCount: Int,
    draggingIndex: MutableState<Int?>,
    dragOffsetY: MutableState<Float>,
    viewModel: RoutineViewModel,
    schedule: RoutineSchedule,
    onEditClick: () -> Unit,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
) {
    val scheduledTask = schedule.tasks[index]
    val timeFormatted = scheduledTask.end.stringTime()
    val shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
    val bgColor = if (isCurrent) {
        LocalCustomColorScheme.current.accentLight
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isCurrent) {
        LocalCustomColorScheme.current.accentDark
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .zIndex(if (draggingIndex.value == index) 1f else 0f),
    ) {
        TimeSegment(
            timeFormatted,
            isTitle = index == routine.data.size - 1,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .dragOffset(
                    index = index,
                    draggingIndex = draggingIndex,
                    dragOffsetY = dragOffsetY,
                )
                .clip(shape)
                .background(color = bgColor)
                .border(1.dp, borderColor, shape)
                .combinedClickable(
                    onClick = { if (isEditMode) onToggleSelect() else onEditClick() },
                    onLongClick = { onToggleSelect() },
                )
                .padding(24.dp, 12.dp, 8.dp, 12.dp),
        ) {
            RoutineTaskSelectionIndicator(
                isEditMode = isEditMode,
                isSelected = isSelected,
                isCompleted = isCompleted,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(link.task.title)
            }

            Box(
                modifier = Modifier.height(TrailingContentHeight),
                contentAlignment = Alignment.Center,
            ) {
                if (isEditMode) {
                    RoutineTaskDragHandle(
                        index = index,
                        itemCount = itemCount,
                        draggingIndex = draggingIndex,
                        dragOffsetY = dragOffsetY,
                        onMove = { from, to -> viewModel.previewReorder(from, to) },
                        onDrop = { viewModel.commitReorder() },
                        onCancel = { viewModel.cancelReorder() },
                    )
                } else {
                    RoutineTaskDurationDropdown(
                        link = link,
                        onDurationSelected = { sub -> viewModel.addOrEditTaskInRoutine(link.copy(subData = sub)) },
                    )
                }
            }
        }
    }
}
