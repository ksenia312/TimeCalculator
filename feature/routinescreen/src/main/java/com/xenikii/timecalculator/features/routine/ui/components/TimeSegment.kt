package com.xenikii.timecalculator.features.routine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TimeSegment(
    time: String,
    isTitle: Boolean = false,
    useSeparator: Boolean = true,
) {
    var style = if (isTitle) MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
    ) else MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    )

    style = style.copy(
        lineHeight = style.fontSize
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(70.dp)
            .offset(y = (style.lineHeight.value / 2 + 4).dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (useSeparator) {
                Separator(weight = 1f)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                time, style = style, textAlign = TextAlign.Center
            )

        }
    }
}

@Composable
private fun ColumnScope.Separator(weight: Float) {
    Box(
        modifier = Modifier
            .weight(weight)
            .width(3.dp)
            .background(
                color = LocalRoutineColor.current, shape = RoundedCornerShape(5.dp)
            )
    )
}