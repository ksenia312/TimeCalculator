package com.xenikii.timecalculator.features.routine.ui.components.tasksselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.shared.components.AppCircleIndicator
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme


@Composable
fun TasksBottomSheetItemHeading(
    modifier: Modifier, links: MutableList<RoutineLink>, task: Task
) {
    val hasTask = links.any { it.task.id == task.id }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val textForeground = @Composable {
            Text(
                links.filter { it.task.id == task.id }.size.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary, lineHeight = 1.sp
                ),
                fontWeight = FontWeight.Bold
            )
        }
        AppCircleIndicator(
            backgroundColor = if (hasTask) LocalCustomColorScheme.current.accent else LocalCustomColorScheme.current.accentLight,
            foregroundColor = LocalCustomColorScheme.current.accent,
            overrideForeground = if (hasTask) textForeground else null
        )
        Text(text = task.title)
    }
}