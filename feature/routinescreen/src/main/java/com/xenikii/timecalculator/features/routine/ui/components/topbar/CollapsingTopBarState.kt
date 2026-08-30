package com.xenikii.timecalculator.features.routine.ui.components.topbar

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Drives a collapsing top bar from the scroll of its content.
 *
 * The [fraction] goes from 0f (fully expanded) to 1f (fully collapsed) as the user scrolls
 * up through [maxOffsetPx] pixels, and expands back when scrolling down. Scroll deltas used
 * for collapsing/expanding are consumed so the bar reacts before the list starts moving.
 */
@Stable
class CollapsingTopBarState(private val maxOffsetPx: Float) {

    private var offsetPx by mutableFloatStateOf(0f)

    val fraction: Float
        get() = if (maxOffsetPx == 0f) 0f else offsetPx / maxOffsetPx

    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val previous = offsetPx
            val newValue = (previous - available.y).coerceIn(0f, maxOffsetPx)
            offsetPx = newValue
            return Offset(0f, previous - newValue)
        }
    }

    /**
     * Lets the top bar itself be dragged to collapse/expand it, so a swipe that starts directly
     * on the bar (e.g. on the routine card) scrolls it instead of doing nothing.
     */
    val scrollableState: ScrollableState = ScrollableState { delta ->
        val previous = offsetPx
        val newValue = (previous - delta).coerceIn(0f, maxOffsetPx)
        offsetPx = newValue
        previous - newValue
    }
}

@Composable
fun rememberCollapsingTopBarState(collapseDistance: Dp = 220.dp): CollapsingTopBarState {
    val maxOffsetPx = with(LocalDensity.current) { collapseDistance.toPx() }
    return remember(maxOffsetPx) { CollapsingTopBarState(maxOffsetPx) }
}
