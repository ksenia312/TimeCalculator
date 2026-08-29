package com.xenikii.timecalculator.features.routine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.features.routine.presentation.RoutineViewModel
import com.xenikii.timecalculator.shared.components.AppCircleIndicator
import com.xenikii.timecalculator.shared.components.AppElevatedButton
import com.xenikii.timecalculator.shared.extensions.draggableItem
import com.xenikii.timecalculator.shared.extensions.stringTime
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme

@OptIn(ExperimentalMaterial3Api::class)
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
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val scheduledTask = schedule.tasks[index]
    val timeFormatted = scheduledTask.end.stringTime()
    val selectedDuration = link.subData?.duration ?: kotlin.time.Duration.ZERO
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
                .draggableItem(
                    index = index,
                    draggingIndex = draggingIndex,
                    dragOffsetY = dragOffsetY,
                    itemCount = itemCount,
                    onMove = { from, to -> viewModel.previewReorder(from, to) },
                    onDrop = { viewModel.commitReorder() },
                    onCancel = { viewModel.cancelReorder() },
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
            val accentColor = LocalRoutineColor.current
            val onAccentColor = LocalRoutineColor.current.copy(alpha = 0.05f)
            AppCircleIndicator(
                backgroundColor = if (isCompleted) accentColor else onAccentColor,
                foregroundColor = if (isCompleted) MaterialTheme.colorScheme.onPrimary else accentColor,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(link.task.title)
            }

            ExposedDropdownMenuBox(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = !menuExpanded },
            ) {
                AppElevatedButton(
                    onClick = { },
                    contentPadding = ButtonDefaults.ContentPadding,
                    modifier = Modifier.menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                        enabled = true,
                    ),
                ) {
                    Box {
                        Text(
                            selectedDuration.takeIf { it > kotlin.time.Duration.ZERO }?.toString()
                                ?: stringResource(R.string.task_set_duration),
                            color = if (selectedDuration > kotlin.time.Duration.ZERO) {
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
                    link.task.dataSortedByDuration.forEach { sub ->
                        DropdownMenuItem(
                            text = {
                                Text("${sub.duration}")
                            },
                            onClick = {
                                menuExpanded = false
                                viewModel.addOrEditTaskInRoutine(
                                    link.copy(
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