package com.xenikii.timecalculator.features.landing.ui.card

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.shared.extensions.stringTime
import com.xenikii.timecalculator.shared.extensions.stringValue
import com.xenikii.timecalculator.shared.features.routineCardBackground
import kotlin.time.Duration
import kotlin.time.Instant

@Composable
fun LandingCardTaskItem(
    @StringRes headerRes: Int,
    remaining: Duration,
    title: String,
    start: Instant,
    end: Instant,
    progress: Float,
    isOngoing: Boolean,
    isCompleted: Boolean,
    routineIsOngoing: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val header = stringResource(headerRes, remaining.stringValue(context))

    val cardBackground = routineCardBackground(
        isOngoing = routineIsOngoing,
        isCompleted = isCompleted,
    )
    val lightenOverlay = if (isOngoing) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.32f)
    }
    val textColor = MaterialTheme.colorScheme.surface
    val subTextColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(cardBackground)
            .background(lightenOverlay)
            .padding(horizontal = 16.dp)
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = header, style = MaterialTheme.typography.labelSmall,
            color = subTextColor
        )
        Text(
            text = title, style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ), color = textColor
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = start.stringTime(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = subTextColor
            )
            Text(
                text = end.stringTime(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = subTextColor
            )
        }

        Spacer(Modifier.height(6.dp))

        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 500),
            label = "progress"
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            color = MaterialTheme.colorScheme.surface,
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.20f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
        )

        Spacer(Modifier.height(6.dp))
    }
}
