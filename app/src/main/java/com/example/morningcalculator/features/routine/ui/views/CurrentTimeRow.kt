package com.example.morningcalculator.features.routine.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CurrentTimeRow(time: String, isTitle: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val style = if (isTitle) MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
        ) else MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Separator(weight = 1f)
        Spacer(Modifier.width(8.dp))
        Text(
            time,
            style = style,
        )
        Spacer(Modifier.width(8.dp))
        Separator(weight = 8f)
    }
}

@Composable
private fun RowScope.Separator(weight: Float) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
    )
}