package com.xenikii.timecalculator.shared.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R

@Composable
fun RoutineCardStatusRow(
    isOngoing: Boolean,
    isCompleted: Boolean,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val alpha = if (isPaused) 0.6f else 0.92f
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(
                    if (isPaused) MaterialTheme.colorScheme.surface.copy(alpha = alpha) else routineStatusDotColor(isOngoing),
                    CircleShape,
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = when {
                isPaused -> stringResource(R.string.routine_paused_label)
                isOngoing -> stringResource(R.string.routine_status_running)
                isCompleted -> stringResource(R.string.routine_status_completed)
                else -> stringResource(R.string.routine_status_planned)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
        )
    }
}