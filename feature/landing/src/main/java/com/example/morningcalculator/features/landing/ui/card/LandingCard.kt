package com.example.morningcalculator.features.landing.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.features.landing.presentation.LandingRoutineState
import com.example.morningcalculator.shared.animation.routineCardSharedKey
import com.example.morningcalculator.shared.features.RoutineCardStatusRow
import com.example.morningcalculator.shared.features.RoutineCardTimeInfo
import com.example.morningcalculator.shared.features.routineCard

@Composable
fun LandingCard(
    routineState: LandingRoutineState,
    onNavigate: (routineId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewItem = routineState.cardViewItem

    Column(
        modifier
            .fillMaxWidth()
            .routineCard(
                viewItem = viewItem,
                sharedKey = routineCardSharedKey(routineState.routineId),
            ) {
                onNavigate(routineState.routineId)
            }
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = viewItem.title, style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ), maxLines = 3, color = MaterialTheme.colorScheme.surface
                )

                Spacer(Modifier.height(8.dp))

                RoutineCardStatusRow(
                    isOngoing = viewItem.isOngoing, isCompleted = viewItem.isCompleted
                )
            }

            Spacer(Modifier.width(16.dp))

            RoutineCardTimeInfo(viewItem, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.weight(1f))

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            val current = routineState.currentTaskViewItem
            val next = routineState.nextTaskViewItem

            if (current != null) {
                LandingCardTaskItem(
                    headerRes = current.headerRes,
                    remaining = current.remaining,
                    title = current.title,
                    start = current.start,
                    end = current.end,
                    progress = current.progress,
                    isOngoing = viewItem.isOngoing,
                    isFirst = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (next != null) {
                Spacer(Modifier.height(12.dp))

                LandingCardTaskItem(
                    headerRes = next.headerRes,
                    remaining = next.remaining,
                    title = next.title,
                    start = next.start,
                    end = next.end,
                    progress = next.progress,
                    isOngoing = viewItem.isOngoing,
                    isFirst = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}