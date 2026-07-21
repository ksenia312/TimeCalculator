package com.example.morningcalculator.features.routine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.morningcalculator.R
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineLink
import com.example.morningcalculator.domain.model.RoutineSchedule
import com.example.morningcalculator.features.routine.presentation.RoutineViewModel
import com.example.morningcalculator.shared.components.AppCircleIndicator
import com.example.morningcalculator.shared.extensions.stringTime
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineTaskItem(
    linkFull: RoutineLink,
    routine: Routine,
    index: Int,
    draggingIndex: MutableState<Int?>,
    dragOffsetY: MutableState<Float>,
    routineLinks: SnapshotStateList<RoutineLink>,
    viewModel: RoutineViewModel,
    schedule: RoutineSchedule,
    onEditClick: () -> Unit,
    isCurrent: Boolean,
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
            .height(IntrinsicSize.Max),
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
                .draggableItem(
                    index = index,
                    draggingIndex = draggingIndex,
                    dragOffsetY = dragOffsetY,
                    routineLinks = routineLinks,
                    viewModel = viewModel,
                )
                .clip(shape)
                .background(
                    color = bgColor
                )
                .border(1.dp, borderColor, shape)
                .clickable {
                    onEditClick()
                }
                .padding(24.dp, 12.dp, 8.dp, 12.dp),
        ) {
            AppCircleIndicator(
                backgroundColor = LocalRoutineColor.current.copy(alpha = 0.05f),
                foregroundColor = LocalRoutineColor.current,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(linkFull.task.title)
            }

            ExposedDropdownMenuBox(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = !menuExpanded },
            ) {
                ElevatedButton(
                    onClick = { },
                    modifier = Modifier.menuAnchor(
                        type = MenuAnchorType.PrimaryEditable,
                        enabled = true,
                    ),
                ) {
                    Box {
                        Text(
                            scheduledTask.duration.takeIf { it > kotlin.time.Duration.ZERO }?.toString()
                                ?: stringResource(R.string.task_set_duration),
                            color = if (scheduledTask.duration > kotlin.time.Duration.ZERO) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        )
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = menuExpanded,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(24.dp),
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    linkFull.task.dataSortedByDuration.forEach { sub ->
                        DropdownMenuItem(
                            text = {
                                Text("${sub.duration}")
                            },
                            onClick = {
                                menuExpanded = false
                                viewModel.addOrEditTaskInRoutine(
                                    linkFull.copy(
                                        subData = sub,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) return
    val element = removeAt(fromIndex)
    add(toIndex, element)
}

@Composable
fun Modifier.draggableItem(
    index: Int,
    draggingIndex: MutableState<Int?>,
    dragOffsetY: MutableState<Float>,
    routineLinks: SnapshotStateList<RoutineLink>,
    viewModel: RoutineViewModel,
): Modifier {
    val isDragging = draggingIndex.value == index
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 72.dp.toPx() }

    return offset {
        IntOffset(
            0,
            if (isDragging) dragOffsetY.value.roundToInt() else 0,
        )
    }
        .zIndex(if (isDragging) 1f else 0f)
        .pointerInput(routineLinks) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    draggingIndex.value = index
                    dragOffsetY.value = 0f
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val current = draggingIndex.value ?: return@detectDragGesturesAfterLongPress
                    val newOffset = dragOffsetY.value + dragAmount.y
                    val delta = (newOffset / itemHeightPx).roundToInt()
                    val target = (current + delta).coerceIn(0, routineLinks.lastIndex)
                    if (target != current) {
                        routineLinks.move(current, target)
                        draggingIndex.value = target
                        dragOffsetY.value = newOffset - delta * itemHeightPx
                    } else {
                        dragOffsetY.value = newOffset
                    }
                },
                onDragEnd = {
                    viewModel.reorderTasks(routineLinks.map { it.id })
                    draggingIndex.value = null
                    dragOffsetY.value = 0f
                },
                onDragCancel = {
                    draggingIndex.value = null
                    dragOffsetY.value = 0f
                },
            )
        }
}