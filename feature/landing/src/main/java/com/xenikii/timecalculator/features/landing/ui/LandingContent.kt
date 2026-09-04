package com.xenikii.timecalculator.features.landing.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.features.landing.presentation.LandingRoutineState
import com.xenikii.timecalculator.features.landing.presentation.LandingState
import com.xenikii.timecalculator.features.landing.ui.card.LandingCardPager
import com.xenikii.timecalculator.shared.components.HomeEmptyState
import com.xenikii.timecalculator.shared.extensions.bottomIndent
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewConstants
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import com.xenikii.timecalculator.shared.viewitem.RoutineCardViewItem
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingContent(
    viewState: LandingState,
    onCreateRoutineClick: () -> Unit = {},
) {
    val navigator = LocalNavigator.current
    val onNavigate: (routineId: String) -> Unit = {
        navigator.navigateTo(AppRoute.Routine(routineId = it))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (viewState) {
            is LandingState.Loading -> {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .bottomIndent()
                )
            }

            is LandingState.Success -> {
                val routineStates = viewState.routineStates
                if (routineStates.isEmpty()) {
                    HomeEmptyState(
                        title = stringResource(R.string.landing_empty_title),
                        subtitle = stringResource(R.string.landing_empty_subtitle),
                        actionText = stringResource(R.string.landing_empty_action),
                        onActionClick = onCreateRoutineClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .bottomIndent()
                            .padding(horizontal = 24.dp),
                    )
                } else {
                    LandingCardPager(
                        modifier = Modifier.fillMaxSize(),
                        routineStates = routineStates,
                        onNavigate = onNavigate,
                    )
                }
            }

            is LandingState.Error -> {
                Text(
                    text = viewState.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .bottomIndent()
                )
            }
        }
    }
}

@PreviewAll
@Composable
fun LandingContentPreview() {
    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    PreviewTheme {
        LandingContent(
            viewState = LandingState.Success(
                routineStates = PreviewConstants.routinesFull.take(3).map { routine ->
                    val schedule = RoutineSchedule(
                        routineId = routine.id,
                        routineTitle = routine.title,
                        effectiveStart = routine.scheduledAt,
                        end = routine.scheduledAt,
                        totalDuration = kotlin.time.Duration.ZERO,
                        tasks = emptyList(),
                        signature = "",
                    )
                    LandingRoutineState(
                        routineId = routine.id,
                        cardViewItem = RoutineCardViewItem(
                            isOngoing = false,
                            isCompleted = false,
                            startLabelRes = R.string.routine_card_will_start,
                            endLabelRes = R.string.routine_card_will_end,
                            startInstant = schedule.effectiveStart,
                            endInstant = schedule.end,
                            title = routine.title,
                            willStartIn = kotlin.time.Duration.ZERO,
                        ),
                        completedTasks = emptyList(),
                        previewTasks = emptyList(),
                        futureTasks = emptyList(),
                        hasHiddenTasks = false,
                    )
                }
            )
        )
    }
}