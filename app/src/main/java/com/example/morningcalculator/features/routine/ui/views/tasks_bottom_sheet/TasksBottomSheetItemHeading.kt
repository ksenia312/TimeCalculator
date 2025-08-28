package com.example.morningcalculator.features.routine.ui.views.tasks_bottom_sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.morningcalculator.core.model.RoutineFullLink
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme


@Composable
fun TasksBottomSheetItemHeading(
    modifier: Modifier, links: MutableList<RoutineFullLink>, task: Task
) {
    val hasTask = links.any { it.task.id == task.id }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (hasTask) LocalCustomColorScheme.current.accent
                    else LocalCustomColorScheme.current.accentLight,
                    shape = RoundedCornerShape(100.dp)
                )
                .size(40.dp), contentAlignment = Alignment.Center
        ) {
            if (hasTask) {
                Text(
                    links.filter { it.task.id == task.id }.size.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary, lineHeight = 1.sp
                    ),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    Icons.Default.Done,
                    contentDescription = "",
                    tint = LocalCustomColorScheme.current.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(text = task.title)
    }
}