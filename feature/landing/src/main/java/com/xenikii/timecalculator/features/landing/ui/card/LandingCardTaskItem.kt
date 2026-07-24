package com.xenikii.timecalculator.features.landing.ui.card

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.shared.extensions.stringTime
import com.xenikii.timecalculator.shared.extensions.stringValue
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val header = stringResource(headerRes, remaining.stringValue(context))

    val cardBg = if (isOngoing) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.16f)
    } else {
        Color.Transparent
    }

    val cardBorderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.16f)
    val textColor = MaterialTheme.colorScheme.surface
    val subTextColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                2.dp, cardBorderColor, RoundedCornerShape(16.dp)
            )
            .padding(10.dp)
            .alpha(if (isCompleted) 0.5f else 1f)
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

        Spacer(Modifier.height(8.dp))

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
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.20f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(50))
        )

        Spacer(Modifier.height(6.dp))
    }
}
