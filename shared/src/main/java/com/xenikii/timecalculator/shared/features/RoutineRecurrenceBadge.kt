package com.xenikii.timecalculator.shared.features

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.RoutineRecurrence
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import java.time.DayOfWeek
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

@Composable
fun RoutineRecurrenceBadge(
    recurrence: RoutineRecurrence,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
) {
    val label = recurrenceBadgeLabel(recurrence) ?: return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.Filled.Repeat,
            contentDescription = null,
            tint = contentColor,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = textStyle,
            color = contentColor,
        )
    }
}

@Composable
private fun recurrenceBadgeLabel(recurrence: RoutineRecurrence): String? {
    val interval = recurrence.interval.coerceAtLeast(1)
    return when (recurrence.unit) {
        RoutineRecurrenceUnit.NONE -> null
        RoutineRecurrenceUnit.DAY -> {
            if (interval == 1) {
                stringResource(R.string.routine_recurrence_summary_daily)
            } else {
                stringResource(
                    R.string.routine_recurrence_summary_every,
                    pluralStringResource(R.plurals.routine_recurrence_days, interval, interval),
                )
            }
        }

        RoutineRecurrenceUnit.WEEK -> {
            val base = if (interval == 1) {
                stringResource(R.string.routine_recurrence_summary_weekly)
            } else {
                stringResource(
                    R.string.routine_recurrence_summary_every,
                    pluralStringResource(R.plurals.routine_recurrence_weeks, interval, interval),
                )
            }
            val daysLabel = recurrenceDaysLabel(recurrence.daysOfWeek)
            if (daysLabel != null) {
                stringResource(R.string.routine_recurrence_summary_on_days, base, daysLabel)
            } else {
                base
            }
        }

        RoutineRecurrenceUnit.MONTH -> {
            if (interval == 1) {
                stringResource(R.string.routine_recurrence_summary_monthly)
            } else {
                stringResource(
                    R.string.routine_recurrence_summary_every,
                    pluralStringResource(R.plurals.routine_recurrence_months, interval, interval),
                )
            }
        }

        RoutineRecurrenceUnit.YEAR -> {
            if (interval == 1) {
                stringResource(R.string.routine_recurrence_summary_yearly)
            } else {
                stringResource(
                    R.string.routine_recurrence_summary_every,
                    pluralStringResource(R.plurals.routine_recurrence_years, interval, interval),
                )
            }
        }
    }
}

@Composable
private fun recurrenceDaysLabel(daysOfWeek: Set<Int>): String? {
    val days = daysOfWeek.filter { it in 1..7 }.toSortedSet()
    if (days.isEmpty()) return null

    if (days == WEEKDAYS) {
        return stringResource(R.string.routine_recurrence_weekdays)
    }

    val configuration = LocalConfiguration.current
    val locale = configuration.locales.get(0) ?: Locale.getDefault()
    return days.joinToString(separator = ", ") { value ->
        DayOfWeek.of(value).getDisplayName(JavaTextStyle.SHORT, locale)
    }
}

private val WEEKDAYS: Set<Int> = sortedSetOf(1, 2, 3, 4, 5)
