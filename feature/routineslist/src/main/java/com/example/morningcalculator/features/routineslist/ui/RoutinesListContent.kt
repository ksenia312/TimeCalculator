package com.example.morningcalculator.features.routineslist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.features.routineslist.presentation.RoutinesListState
import com.example.morningcalculator.features.routineslist.ui.components.RoutineListItem
import com.example.morningcalculator.shared.components.HomeEmptyState
import com.example.morningcalculator.shared.extensions.bottomIndent
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewConstants
import com.example.morningcalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesListContent(
    viewState: RoutinesListState,
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
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        item { Spacer(Modifier.height(16.dp)) }
                        items.forEach { routineItem ->
                            item(key = routineItem.routine.id) {
                                RoutineListItem(
                                    item = routineItem,
                                )
                            }
                        }
                        item { Box(Modifier.bottomIndent()) }
                    }
                }
            }

            is RoutinesListState.Error -> {
                val viewState = viewState
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
                    com.example.morningcalculator.features.routineslist.presentation.RoutineListItemState(
                        routine = it,
                        schedule = com.example.morningcalculator.domain.model.RoutineSchedule(
                            routineId = it.id,
                            routineTitle = it.title,
                            effectiveStart = it.scheduledAt,
                            end = it.scheduledAt,
                            totalDuration = kotlin.time.Duration.ZERO,
                            tasks = emptyList(),
                            signature = "",
                        ),
                        cardViewItem = com.example.morningcalculator.shared.viewitem.RoutineCardViewItem(
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
            onCreateRoutineClick = {},
        )
    }
}