package com.xenikii.timecalculator.features.routineeditor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val WeekDays: List<DayOfWeek> = DayOfWeek.entries

@Composable
fun RoutineDaysOfWeekSelector(
    selectedDays: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentLocale()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WeekDays.forEach { day ->
            val value = day.value
            DayCircle(
                label = day.getDisplayName(TextStyle.NARROW, locale),
                selected = selectedDays.contains(value),
                onClick = { onToggle(value) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayCircle(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = containerColor, shape = CircleShape)
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    )
                }
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun currentLocale(): Locale {
    val configuration = LocalConfiguration.current
    return configuration.locales.get(0) ?: Locale.getDefault()
}

@Preview
@Composable
private fun RoutineDaysOfWeekSelectorPreview() {
    PreviewTheme {
        RoutineDaysOfWeekSelector(
            selectedDays = setOf(1, 3, 5),
            onToggle = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}