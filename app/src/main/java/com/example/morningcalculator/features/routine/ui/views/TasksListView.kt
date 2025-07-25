package com.example.morningcalculator.features.routine.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.morningcalculator.core.model.RoutineCombined
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.features.routine.ui.views.task_dialog.EditTaskDialog
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import com.example.morningcalculator.shared.extensions.timeOnMoment
import com.example.morningcalculator.shared.extensions.whenToGetUp
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.TasksListView(
    combined: RoutineCombined, viewModel: RoutineViewModel
) {
    val whenToGetUp = combined.whenToGetUp()
    // 1) snapshotStateList для перерисовки
    val taskPairs = remember { combined.taskPairs.toMutableStateList() }

    // 2) состояние drag
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    val editingTask = remember { mutableStateOf<Pair<Task, String>?>(null) }
    if (editingTask.value != null) {
        val task = editingTask.value!!
        EditTaskDialog(
            initialTask = task.first,
            initialSubDataId = task.second,
            onDismiss = { editingTask.value = null },
            onConfirm = { request, selectedIndex ->
                viewModel.editTask(
                    request, selectedIndex
                )
            },
        )
    }

    LazyColumn(modifier = Modifier.weight(1f)) {
        // шапка
        item(key = "wakeUp") {
            CurrentTimeRow(whenToGetUp.toString(), isTitle = true)
        }

        // 3) список с drag-and-drop
        itemsIndexed(
            items = taskPairs,
            key = { _, (task, _) -> task.id }) { index, (task, selectedSubData) ->

            var menuExpanded by remember { mutableStateOf(false) }
            var current by remember(selectedSubData) {
                mutableStateOf(selectedSubData)
            }

            // флаг — это именно наша текущая перетаскиваемая ячейка?
            val isDragging = draggingIndex == index

            // высота одной ячейки (примерно), нужна для вычисления, когда менять местами
            val itemHeightPx = with(density) { 72.dp.toPx() }

            val time = combined.timeOnMoment(index)
            // обёртка, которая смещается и реагирует на жесты
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // смещение только у активного элемента
                    .offset { IntOffset(0, if (isDragging) dragOffsetY.roundToInt() else 0) }
                    .zIndex(if (isDragging) 1f else 0f)
                    .pointerInput(index) {
                        detectDragGesturesAfterLongPress(onDragStart = {
                            draggingIndex = index
                        }, onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetY += dragAmount.y

                            // когда сместились более чем на высоту одной ячейки — меняем местами
                            val targetIndex =
                                (index + (dragOffsetY / itemHeightPx).roundToInt()).coerceIn(
                                    0, taskPairs.lastIndex
                                )

                            if (draggingIndex != null && targetIndex != draggingIndex) {
                                taskPairs.swap(draggingIndex!!, targetIndex)
                                draggingIndex = targetIndex
                                // скорректируем offset, чтобы не “прыгало”
                                dragOffsetY -= (targetIndex - index) * itemHeightPx
                            }
                        }, onDragEnd = {
                            viewModel.reorderTasks(taskPairs.map { it.first.id })
                            draggingIndex = null
                            dragOffsetY = 0f
                        }, onDragCancel = {
                            draggingIndex = null
                            dragOffsetY = 0f
                        })
                    }) {
                Column {
                    ListItem(modifier = Modifier.clickable {
                        editingTask.value = (task to current.id)
                    }, headlineContent = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(task.title)
                        }
                    }, trailingContent = {
                        ExposedDropdownMenuBox(
                            expanded = menuExpanded,
                            onExpandedChange = { menuExpanded = !menuExpanded }) {
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
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }) {
                                task.data.sortedBy { it.duration }.forEach { sub ->
                                    DropdownMenuItem(text = {
                                        Text("${sub.duration}")
                                    }, onClick = {
                                        current = sub
                                        menuExpanded = false
                                        viewModel.addOrEditTaskInRoutine(task, sub)
                                    })
                                }
                            }
                        }
                    })
                    CurrentTimeRow(time.toString(), isTitle = index == combined.taskPairs.size - 1)

                }
            }
        }
    }
}

private fun <T> MutableList<T>.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}