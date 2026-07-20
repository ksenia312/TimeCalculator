package com.example.morningcalculator.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R

@Composable
fun AppTextIconButton(
    painter: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    foregroundColor: Color = MaterialTheme.colorScheme.primary
) {
    TextButton(
        modifier = modifier.fillMaxWidth(), onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter,
                contentDescription = "",
                colorFilter = ColorFilter.tint(foregroundColor)
            )
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = foregroundColor
                ),
            )
        }
    }
}

@Composable
fun AddNewButton(
    text: String,
    modifier: Modifier = Modifier,
    foregroundColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    AppTextIconButton(
        foregroundColor = foregroundColor,
        painter = painterResource(R.drawable.add_circle),
        text = text,
        onClick = onClick,
        modifier = modifier
    )
}