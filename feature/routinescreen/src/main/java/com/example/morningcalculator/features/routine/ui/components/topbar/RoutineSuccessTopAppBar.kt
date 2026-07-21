package com.example.morningcalculator.features.routine.ui.components.topbar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineSchedule
import com.example.morningcalculator.shared.viewitem.RoutineCardViewItem
import com.example.morningcalculator.features.routine.presentation.RoutineViewState
import com.example.morningcalculator.shared.animation.routineCardSharedKey
import com.example.morningcalculator.shared.components.CustomTopBar
import com.example.morningcalculator.shared.features.routineCard
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineSuccessTopAppBar(
    viewState: RoutineViewState.Success,
    onShowEditDialog: () -> Unit = { },
) {
    val viewItem = viewState.cardViewItem

    CustomTopBar(
        onAccentColor = Color.White,
        modifier = Modifier
            .routineCard(
                verticalPadding = 24.dp,
                horizontalPadding = 0.dp,
                viewItem = viewItem,
                sharedKey = routineCardSharedKey(viewState.routine.id),
                shape = RoundedCornerShape(
                    bottomEnd = 28.dp,
                    bottomStart = 28.dp
                )
            ) {
                onShowEditDialog()
            },
        showNavigationIcon = true,
    ) {
        RoutineCard(viewItem)
    }
}

@PreviewAll
@Composable
fun RoutineSuccessTopAppBarPreview() {
    val routine = Routine(
        id = "1",
        title = stringResource(R.string.sample_morning_routine),
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