package com.example.morningcalculator.features.landing.ui.card

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
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.landing.ui.currentTaskIndex
import com.example.morningcalculator.features.landing.ui.viewitem.LandingCardTaskViewItem
import com.example.morningcalculator.features.landing.ui.viewitem.RoutineCardViewItem
import com.example.morningcalculator.shared.features.RoutineCardStatusRow
import com.example.morningcalculator.shared.features.RoutineCardTimeInfo
import com.example.morningcalculator.shared.features.routineCard
import kotlin.time.Instant

@Composable
fun LandingCard(
    routine: Routine,
    onNavigate: (routine: Routine) -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    val viewItem = RoutineCardViewItem.create(routine = routine)

    Column(
        modifier
            .fillMaxWidth()
            .routineCard(viewItem = viewItem) {
                onNavigate(routine)
            }) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title, style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ), maxLines = 3, color = MaterialTheme.colorScheme.surface
                )

                Spacer(Modifier.height(8.dp))

                RoutineCardStatusRow(
                    isOngoing = viewItem.isOngoing, isCompleted = viewItem.isCompleted
                )
            }

            Spacer(Modifier.width(16.dp))

            RoutineCardTimeInfo(viewItem)
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Bottom
        ) {
            val taskCount = routine.data.size
            val isOngoing = viewItem.isOngoing
            val currentIndex = when {
                taskCount == 0 -> null
                isOngoing -> currentTaskIndex(routine, now)
                else -> 0
            }

            val nextIndex = when {
                taskCount == 0 -> null
                isOngoing -> currentIndex?.plus(1)
                else -> 1
            }?.takeIf { it in 0 until taskCount }

            if (currentIndex != null) {
                val current = LandingCardTaskViewItem.create(
                    routine = routine, index = currentIndex, now = now
                )

                LandingCardTaskItem(
                    header = current.header,
                    title = current.title,
                    start = current.start,
                    end = current.end,
                    progress = current.progress,
                    isOngoing = isOngoing,
                    isFirst = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (nextIndex != null) {
                Spacer(Modifier.height(12.dp))

                val next =
                    LandingCardTaskViewItem.create(routine = routine, index = nextIndex, now = now)

                LandingCardTaskItem(
                    header = next.header,
                    title = next.title,
                    start = next.start,
                    end = next.end,
                    progress = next.progress,
                    isOngoing = isOngoing,
                    isFirst = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}