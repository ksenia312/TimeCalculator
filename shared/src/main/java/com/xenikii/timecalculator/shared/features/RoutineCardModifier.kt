package com.xenikii.timecalculator.shared.features

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.shared.animation.LocalCardAnimatedContentScope
import com.xenikii.timecalculator.shared.animation.LocalSharedTransitionScope
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigationBackStack
import com.xenikii.timecalculator.shared.viewitem.RoutineCardViewItem

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
    val currentRoute = LocalNavigationBackStack.current.lastOrNull() as? AppRoute

    val sharedModifier =
        if (
            sharedKey != null &&
            currentRoute is AppRoute.Routine &&
            sharedScope != null &&
            animatedScope != null
        ) {
            with(sharedScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = sharedKey),
                    animatedVisibilityScope = animatedScope,
                    clipInOverlayDuringTransition = OverlayClip(shape),
                    boundsTransform = BoundsTransform { _, _ -> tween(durationMillis = 100) },
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
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
        .padding(horizontalPadding, verticalPadding)
}