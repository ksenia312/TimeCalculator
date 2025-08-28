package com.example.morningcalculator.features.routine.ui.views.tasks_bottom_sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.RoutineFullLink
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme

@Composable
fun TasksBottomSheetItemTrailing(links: MutableList<RoutineFullLink>, task: Task) {
    val hasTask = links.any { it.task.id == task.id }
    Box(Modifier.fillMaxHeight()) {
        AnimatedVisibility(visible = hasTask) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallIconButton(
                    onClick = {
                        val el = links.firstOrNull { it.task.id == task.id }
                        if (el != null) links.remove(el)
                    }) {
                    Image(
                        painter = painterResource(id = R.drawable.remove_circle),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(LocalCustomColorScheme.current.label)
                    )
                }
                SmallIconButton(
                    onClick = {
                        links.addTask(task)
                    }) {
                    Image(
                        painter = painterResource(id = R.drawable.add_circle),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(LocalCustomColorScheme.current.label)
                    )
                }
            }
        }
    }
}


@Composable
fun SmallIconButton(
    onClick: () -> Unit,
    size: Dp = 32.dp,
    padding: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}