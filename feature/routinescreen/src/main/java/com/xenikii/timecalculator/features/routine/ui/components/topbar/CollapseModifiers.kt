package com.xenikii.timecalculator.features.routine.ui.components.topbar

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

/**
 * Shrinks the measured height of the content towards zero as [fraction] goes 0f -> 1f,
 * clipping any overflow. Purely a layout collapse, without changing opacity.
 */
fun Modifier.collapseVertically(fraction: Float): Modifier {
    val f = fraction.coerceIn(0f, 1f)
    return this
        .graphicsLayer { clip = true }
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val height = (placeable.height * (1f - f)).roundToInt().coerceAtLeast(0)
            layout(placeable.width, height) {
                placeable.place(0, 0)
            }
        }
}

/**
 * Collapses the content both in height and opacity as [fraction] goes 0f -> 1f.
 * The fade finishes earlier than the height collapse (controlled by [fadeMultiplier])
 * so the content is gone before the space it occupied fully disappears.
 */
fun Modifier.fadeCollapse(fraction: Float, fadeMultiplier: Float = 1.6f): Modifier {
    val f = fraction.coerceIn(0f, 1f)
    return this
        .graphicsLayer { alpha = (1f - f * fadeMultiplier).coerceIn(0f, 1f) }
        .collapseVertically(f)
}
