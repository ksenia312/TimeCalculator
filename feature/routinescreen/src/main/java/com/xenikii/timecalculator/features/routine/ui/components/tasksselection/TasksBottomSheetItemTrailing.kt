package com.xenikii.timecalculator.features.routine.ui.components.tasksselection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.shared.components.SmallIconButton
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme

@Composable
fun TasksBottomSheetItemTrailing(links: MutableList<RoutineLink>, task: Task) {
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

