package com.example.morningcalculator.features.landing.ui.viewitem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.shared.extensions.stringTime
import kotlin.time.Instant

@Composable
fun LandingCardTaskItem(
    header: String,
    title: String,
    start: Instant,
    end: Instant,
    progress: Float,
    isOngoing: Boolean,
    isFirst: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardBg = if (isOngoing && isFirst) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.16f)
    } else {
        Color.Transparent
    }

    val cardBorderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.16f)
    val textColor = MaterialTheme.colorScheme.surface
    val subTextColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(
                2.dp, cardBorderColor, RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = header, style = MaterialTheme.typography.labelLarge, color = subTextColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = title, style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ), color = textColor
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = start.stringTime(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = subTextColor
            )
            Text(
                text = end.stringTime(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = subTextColor
            )
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.20f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}
