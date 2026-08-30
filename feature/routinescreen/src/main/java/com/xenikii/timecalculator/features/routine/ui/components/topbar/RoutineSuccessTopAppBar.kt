package com.xenikii.timecalculator.features.routine.ui.components.topbar

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.features.routine.presentation.RoutineViewState
import com.xenikii.timecalculator.shared.animation.routineCardSharedKey
import com.xenikii.timecalculator.shared.features.routineCard
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import com.xenikii.timecalculator.shared.viewitem.RoutineCardViewItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineSuccessTopAppBar(
    viewState: RoutineViewState.Success,
    collapseFraction: Float = 0f,
    scrollableState: ScrollableState? = null,
    onShowEditDialog: () -> Unit = { },
) {
    val fraction = collapseFraction.coerceIn(0f, 1f)
    val viewItem = viewState.cardViewItem

    val cardFade = ((fraction - 0.1f) / 0.35f).coerceIn(0f, 1f)
    val cardCollapse = (fraction / 0.85f).coerceIn(0f, 1f)
    val titleEnter = ((fraction - 0.5f) / 0.5f).coerceIn(0f, 1f)
    val collapsedOwnsTitle = titleEnter > 0f

    Column(
        modifier = Modifier
            .routineCard(
                verticalPadding = if (collapsedOwnsTitle) PaddingValues(top = 8.dp, bottom = 12.dp) else PaddingValues(top = 8.dp, bottom = 16.dp),
                horizontalPadding = PaddingValues(0.dp),
                viewItem = viewItem,
                sharedKey = routineCardSharedKey(viewState.routine.id),
                shape = RoundedCornerShape(
                    bottomEnd = 28.dp,
                    bottomStart = 28.dp
                ),
            )
            .then(
                if (scrollableState != null) {
                    Modifier.scrollable(
                        state = scrollableState,
                        orientation = Orientation.Vertical,
                    )
                } else {
                    Modifier
                }
            )
            .statusBarsPadding()
    ) {
        RoutineCollapsingHeader(
            title = viewItem.title,
            collapsedTitleAlpha = titleEnter,
            ownsTitleForA11y = collapsedOwnsTitle,
            onSettingsClick = onShowEditDialog,
        )

        Spacer(Modifier.height(lerp(16.dp, 0.dp, fraction)))

        RoutineCard(
            viewItem = viewItem,
            collapseFraction = fraction,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .graphicsLayer { alpha = 1f - cardFade }
                .collapseVertically(cardCollapse)
                .then(
                    if (collapsedOwnsTitle) Modifier.clearAndSetSemantics { } else Modifier
                ),
        )
    }
}

@PreviewAll
@Composable
fun RoutineSuccessTopAppBarPreview() {
    val routine = Routine(
        id = "1",
        title = stringResource(R.string.sample_time_routine),
        modifiedAt = System.currentTimeMillis(),
        color = "0xFF599AC9",
        scheduledAt = kotlin.time.Instant.fromEpochMilliseconds(System.currentTimeMillis()),
        data = listOf()
    )
    val viewState = RoutineViewState.Success(
        routine = routine,
        schedule = RoutineSchedule(
            routineId = routine.id,
            routineTitle = routine.title,
            effectiveStart = routine.scheduledAt,
            end = routine.scheduledAt,
            totalDuration = kotlin.time.Duration.ZERO,
            tasks = emptyList(),
            signature = "",
        ),
        cardViewItem = RoutineCardViewItem(
            isOngoing = false,
            isCompleted = false,
            startLabelRes = R.string.routine_card_will_start,
            endLabelRes = R.string.routine_card_will_end,
            startInstant = routine.scheduledAt,
            endInstant = routine.scheduledAt,
            title = routine.title,
            willStartIn = kotlin.time.Duration.ZERO,
        ),
        currentTaskIndex = null,
    )
    PreviewTheme {
        RoutineSuccessTopAppBar(
            viewState = viewState,
        )
    }
}