package com.xenikii.timecalculator.features.routineslist.ui

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
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListState
import com.xenikii.timecalculator.features.routineslist.ui.components.RoutinesLazyList
import com.xenikii.timecalculator.shared.components.HomeEmptyState
import com.xenikii.timecalculator.shared.extensions.bottomIndent
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewConstants
import com.xenikii.timecalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesListContent(
    viewState: RoutinesListState,
    selectedIds: Set<String>,
    onLongPress: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onCreateRoutineClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (viewState) {
            is RoutinesListState.Loading -> {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .bottomIndent()
                )
            }

            is RoutinesListState.Success -> {
                val items = viewState.items
                if (items.isEmpty()) {
                    HomeEmptyState(
                        title = stringResource(R.string.routines_empty_title),
                        subtitle = stringResource(R.string.routines_empty_subtitle),
                        actionText = stringResource(R.string.routines_empty_action),
                        onActionClick = onCreateRoutineClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .bottomIndent()
                            .padding(horizontal = 24.dp)
                    )
                } else {
                    RoutinesLazyList(
                        items = items,
                        selectedIds = selectedIds,
                        onLongPress = onLongPress,
                        onToggleSelect = onToggleSelect,
                    )
                }
            }

            is RoutinesListState.Error -> {
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
fun RoutineListContentPreview() {
    PreviewTheme {
        RoutinesListContent(
            viewState = RoutinesListState.Success(
                items = PreviewConstants.routinesFull.map {
                    com.xenikii.timecalculator.features.routineslist.presentation.RoutineListItemState(
                        routine = it,
                        schedule = com.xenikii.timecalculator.domain.model.RoutineSchedule(
                            routineId = it.id,
                            routineTitle = it.title,
                            effectiveStart = it.scheduledAt,
                            end = it.scheduledAt,
                            totalDuration = kotlin.time.Duration.ZERO,
                            tasks = emptyList(),
                            signature = "",
                        ),
                        cardViewItem = com.xenikii.timecalculator.shared.viewitem.RoutineCardViewItem(
                            isOngoing = false,
                            isCompleted = false,
                            startLabelRes = R.string.routine_card_will_start,
                            endLabelRes = R.string.routine_card_will_end,
                            startInstant = it.scheduledAt,
                            endInstant = it.scheduledAt,
                            title = it.title,
                            willStartIn = kotlin.time.Duration.ZERO,
                        ),
                    )
                },
                sort = RoutinesListState.Sort.DEFAULT
            ),
            selectedIds = emptySet(),
            onLongPress = {},
            onToggleSelect = {},
            onCreateRoutineClick = {},
        )
    }
}