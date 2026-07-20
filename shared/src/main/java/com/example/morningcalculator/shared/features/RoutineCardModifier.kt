package com.example.morningcalculator.shared.features

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.shared.animation.LocalCardAnimatedContentScope
import com.example.morningcalculator.shared.animation.LocalSharedTransitionScope
import com.example.morningcalculator.shared.viewitem.RoutineCardViewItem

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.routineCard(
    horizontalPadding: Dp = 24.dp,
    verticalPadding: Dp = 32.dp,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    viewItem: RoutineCardViewItem,
    sharedKey: Any? = null,
    onClick: () -> Unit,
): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalCardAnimatedContentScope.current

    val sharedModifier =
        if (sharedKey != null && sharedScope != null && animatedScope != null) {
            with(sharedScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = sharedKey),
                    animatedVisibilityScope = animatedScope,
                    clipInOverlayDuringTransition = OverlayClip(shape),
                    boundsTransform = BoundsTransform { _, _ -> tween(durationMillis = 150) },
                )
            }
        } else {
            Modifier
        }

    return this
        .then(sharedModifier)
        .fillMaxWidth()
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)
        .background(
            routineCardBackground(
                isOngoing = viewItem.isOngoing,
                isCompleted = viewItem.isCompleted
            )
        )
        .clickable(onClick = onClick)
        .padding(horizontalPadding, verticalPadding)
}