package com.example.morningcalculator.shared.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.features.landing.ui.viewitem.RoutineCardViewItem

@Composable
fun RoutineCardTimeInfo(
    viewItem: RoutineCardViewItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        if (!viewItem.isOngoing && !viewItem.isCompleted) {
            Text(
                text = stringResource(R.string.routine_time_will_start_in),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                textAlign = TextAlign.End
            )
            Text(
                text = viewItem.willStartIn,
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(10.dp))
        }

        Text(
            text = viewItem.startLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            textAlign = TextAlign.End
        )
        Text(
            text = viewItem.startText, style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ), color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.End
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = viewItem.endLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            textAlign = TextAlign.End
        )
        Text(
            text = viewItem.endText, style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ), color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.End
        )
    }
}