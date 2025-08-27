package com.example.morningcalculator.features.routine.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
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
    index: Int,
    draggingIndex: MutableState<Int?>,
    full: Full,
    dragOffsetY: MutableState<Float>,
    routineFullLinks: SnapshotStateList<RoutineFullLink>,
    viewModel: RoutineViewModel,
    editingLink: MutableState<RoutineFullLink?>
) {
    val density = LocalDensity.current
    var menuExpanded by remember { mutableStateOf(false) }
    var current by remember(linkFull.subData) {
        mutableStateOf(linkFull.subData)
    }
    val isDragging = draggingIndex.value == index
    val itemHeightPx = with(density) { 72.dp.toPx() }

    val time = full.timeOnMoment(index)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, if (isDragging) dragOffsetY.value.roundToInt() else 0) }
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
            }) {
        Column {
            ListItem(modifier = Modifier.clickable {
                editingLink.value = linkFull
            }, headlineContent = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(linkFull.task.title)
                }
            }, trailingContent = {
                ExposedDropdownMenuBox(
                    expanded = menuExpanded, onExpandedChange = { menuExpanded = !menuExpanded }) {
                    ElevatedButton(
                        onClick = { }, modifier = Modifier.menuAnchor(
                            type = MenuAnchorType.PrimaryEditable, enabled = true
                        )
                    ) {
                        Row {
                            Text(current.duration.toString())
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = menuExpanded
                            )
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        linkFull.task.data.sortedBy { it.duration }.forEach { sub ->
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
            })
            CurrentTimeRow(time.toString(), isTitle = index == full.data.size - 1)
        }
    }
}

private fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) return
    val element = removeAt(fromIndex)
    add(toIndex, element)
}