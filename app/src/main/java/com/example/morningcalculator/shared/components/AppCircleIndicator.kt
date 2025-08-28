package com.example.morningcalculator.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppCircleIndicator(
    backgroundColor: Color,
    foregroundColor: Color,
    overrideForeground: (@Composable () -> Unit)? = null,
    size: Dp = 32.dp
) {
    Box(
        modifier = Modifier
            .background(
                backgroundColor,
                shape = CircleShape
            )
            .size(size), contentAlignment = Alignment.Center
    ) {
        if (overrideForeground != null) {
            overrideForeground()
        } else {
            Icon(
                Icons.Default.Done,
                contentDescription = "",
                tint = foregroundColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}