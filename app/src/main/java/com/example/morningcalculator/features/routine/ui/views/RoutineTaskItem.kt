package com.example.morningcalculator.features.routine.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.morningcalculator.core.model.Routine.Full
import com.example.morningcalculator.core.model.RoutineFullLink
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import com.example.morningcalculator.shared.extensions.timeOnMoment
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineTaskItem(
    linkFull: RoutineFullLink,
    routineFull: Full,
    index: Int,
    draggingIndex: MutableState<Int?>,
    dragOffsetY: MutableState<Float>,
    routineFullLinks: SnapshotStateList<RoutineFullLink>,
    viewModel: RoutineViewModel,
    editingLink: MutableState<RoutineFullLink?>,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var current by remember(linkFull.subData) {
        mutableStateOf(linkFull.subData)
    }

    val time = routineFull.timeOnMoment(index)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
    ) {
        TimeSegment(
            time.toString(), isTitle = index == routineFull.data.size - 1
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .draggableItem(
                    index = index,
                    draggingIndex = draggingIndex,
                    dragOffsetY = dragOffsetY,
                    routineFullLinks = routineFullLinks,
                    viewModel = viewModel
                )
                .clip(RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp))
                .background(
                    color = MaterialTheme.colorScheme.surface
                )
                .clickable {
                    editingLink.value = linkFull
                }
                .padding(24.dp, 12.dp, 8.dp, 12.dp)) {
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
                onExpandedChange = { menuExpanded = !menuExpanded }) {
                ElevatedButton(
                    onClick = { }, modifier = Modifier.menuAnchor(
                        type = MenuAnchorType.PrimaryEditable, enabled = true
                    )
                ) {
                    Box {
                        Text(current.duration.toString())
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = menuExpanded,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(24.dp)
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    linkFull.task.dataSortedByDuration.forEach { sub ->
                        DropdownMenuItem(text = {
                            Text("${sub.duration}")
                        }, onClick = {
                            current = sub
                            menuExpanded = false
                            viewModel.addOrEditTaskInRoutine(
                                linkFull.copy(
                                    subData = sub
                                )
                            )
                        })
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
    routineFullLinks: SnapshotStateList<RoutineFullLink>,
    viewModel: RoutineViewModel
): Modifier {
    val isDragging = draggingIndex.value == index
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 72.dp.toPx() }

    return offset {
        IntOffset(
            0, if (isDragging) dragOffsetY.value.roundToInt() else 0
        )
    }
        .zIndex(if (isDragging) 1f else 0f)
        .pointerInput(routineFullLinks) {
            detectDragGesturesAfterLongPress(onDragStart = {
                draggingIndex.value = index
                dragOffsetY.value = 0f
            }, onDrag = { change, dragAmount ->
                change.consume()
                val current = draggingIndex.value ?: return@detectDragGesturesAfterLongPress
                val newOffset = dragOffsetY.value + dragAmount.y
                val delta = (newOffset / itemHeightPx).roundToInt()
                val target = (current + delta).coerceIn(0, routineFullLinks.lastIndex)
                if (target != current) {
                    routineFullLinks.move(current, target)
                    draggingIndex.value = target
                    dragOffsetY.value = newOffset - delta * itemHeightPx
                } else {
                    dragOffsetY.value = newOffset
                }
            }, onDragEnd = {
                viewModel.reorderTasks(routineFullLinks.map { it.id })
                draggingIndex.value = null
                dragOffsetY.value = 0f
            }, onDragCancel = {
                draggingIndex.value = null
                dragOffsetY.value = 0f
            })
        }
}