package com.xenikii.timecalculator.features.routine.ui.components.topbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.xenikii.timecalculator.domain.model.RoutineRecurrenceUnit
import com.xenikii.timecalculator.shared.features.RoutineCardStatusRow
import com.xenikii.timecalculator.shared.features.RoutineCardTimeInfo
import com.xenikii.timecalculator.shared.features.RoutineRecurrenceBadge
import com.xenikii.timecalculator.shared.viewitem.RoutineCardViewItem

@Composable
fun RoutineCard(
    viewItem: RoutineCardViewItem,
    modifier: Modifier = Modifier,
    collapseFraction: Float = 0f,
) {
    val fraction = collapseFraction.coerceIn(0f, 1f)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = viewItem.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 3,
                color = MaterialTheme.colorScheme.surface
            )

            Column(modifier = Modifier.fadeCollapse(fraction)) {
                Spacer(Modifier.height(8.dp))

                if (viewItem.recurrence.unit != RoutineRecurrenceUnit.NONE) {
                    RoutineRecurrenceBadge(
                        recurrence = viewItem.recurrence,
                        contentColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        textStyle = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(Modifier.height(8.dp))
                }

                RoutineCardStatusRow(
                    isOngoing = viewItem.isOngoing,
                    isCompleted = viewItem.isCompleted
                )
            }
        }
        Spacer(Modifier.width(lerp(16.dp, 0.dp, fraction)))

        RoutineCardTimeInfo(
            viewItem,
            modifier = Modifier
                .weight((1f - fraction).coerceAtLeast(0.0001f))
                .fadeCollapse(fraction)
        )
    }
}