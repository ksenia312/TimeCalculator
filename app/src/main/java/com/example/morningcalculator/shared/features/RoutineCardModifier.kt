package com.example.morningcalculator.shared.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.features.landing.ui.viewitem.RoutineCardViewItem

@Composable
fun Modifier.routineCard(
    horizontalPadding: Dp = 24.dp,
    verticalPadding: Dp = 32.dp,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    viewItem: RoutineCardViewItem,
    onClick: () -> Unit,
) =
    fillMaxWidth()
        .clip(shape)
        .background(
            routineCardBackground(
                isOngoing = viewItem.isOngoing,
                isCompleted = viewItem.isCompleted
            )
        )
        .padding(horizontalPadding, verticalPadding)
        .clickable(onClick = onClick)