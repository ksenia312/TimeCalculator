package com.xenikii.timecalculator.shared.extensions

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.shared.composition.LocalBottomIndent
import kotlin.math.roundToInt

@Composable
fun Modifier.bottomIndent(): Modifier = this.padding(bottom = LocalBottomIndent.current)

/**
 * Applies the visual offset for the item currently being dragged in a reorderable list.
 * Put this on the item's row; pair it with [dragHandle] on the affordance that actually
 * drives [draggingIndex]/[dragOffsetY].
 */
@Composable
fun Modifier.dragOffset(
    index: Int,
    draggingIndex: MutableState<Int?>,
    dragOffsetY: MutableState<Float>,
): Modifier = this.offset {
    IntOffset(0, if (draggingIndex.value == index) dragOffsetY.value.roundToInt() else 0)
}

/**
 * Makes a drag handle reorder the list it belongs to as it's dragged.
 *
 * The modifier owns none of the domain state — it only reports intent:
 *  - [itemCount]: total number of items, used to clamp the drag target.
 *  - [onMove]: item crossed into a new slot (from -> to). The caller is
 *    responsible for producing the new order (e.g. mutating a list or
 *    updating a draft in a ViewModel).
 *  - [onDrop]: drag finished successfully — persist the current order.
 *  - [onCancel]: drag was interrupted — revert to the last committed order.
 *
 * @param itemHeight height of a single row, used to translate pixel offsets
 *   into index deltas.
 */
@Composable
fun Modifier.dragHandle(
    index: Int,
    draggingIndex: MutableState<Int?>,
    dragOffsetY: MutableState<Float>,
    itemCount: Int,
    itemHeight: Dp = 72.dp,
    onMove: (from: Int, to: Int) -> Unit,
    onDrop: () -> Unit,
    onCancel: () -> Unit = {},
): Modifier {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val currentIndex by rememberUpdatedState(index)
    val currentItemCount by rememberUpdatedState(itemCount)
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnDrop by rememberUpdatedState(onDrop)
    val currentOnCancel by rememberUpdatedState(onCancel)

    return this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                draggingIndex.value = currentIndex
                dragOffsetY.value = 0f
            },
            onDrag = { change, dragAmount ->
                change.consume()
                val current = draggingIndex.value ?: return@detectDragGestures
                val newOffset = dragOffsetY.value + dragAmount.y
                val delta = (newOffset / itemHeightPx).roundToInt()
                val target = (current + delta).coerceIn(0, currentItemCount - 1)
                if (target != current) {
                    currentOnMove(current, target)
                    draggingIndex.value = target
                    dragOffsetY.value = newOffset - delta * itemHeightPx
                } else {
                    dragOffsetY.value = newOffset
                }
            },
            onDragEnd = {
                currentOnDrop()
                draggingIndex.value = null
                dragOffsetY.value = 0f
            },
            onDragCancel = {
                currentOnCancel()
                draggingIndex.value = null
                dragOffsetY.value = 0f
            },
        )
    }
}