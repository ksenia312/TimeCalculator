package com.xenikii.timecalculator.features.routine.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineSchedule
import com.xenikii.timecalculator.features.routine.presentation.RoutineViewModel
import com.xenikii.timecalculator.shared.extensions.stringTime
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.EditTaskArguments
import com.xenikii.timecalculator.shared.navigator.EditTaskSource
import com.xenikii.timecalculator.shared.navigator.LocalNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.TasksListView(
    routine: Routine,
    schedule: RoutineSchedule,
    viewModel: RoutineViewModel,
    currentTaskIndex: Int?,
) {
    val navigator = LocalNavigator.current
    val links = routine.data

    if (links.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.Center)
        ) {
            Text(
                text = stringResource(R.string.routine_tasks_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.routine_tasks_empty_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }
        return
    }


    val whenToGetUp = schedule.effectiveStart.stringTime()
    val draggingIndex = remember { mutableStateOf<Int?>(null) }
    val dragOffsetY = remember { mutableFloatStateOf(0f) }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item { Spacer(Modifier.height(12.dp)) }
        item(key = "wakeUp") {
            TimeSegment(
                whenToGetUp,
                isTitle = true,
                useSeparator = false,
            )
        }
        itemsIndexed(
            items = links,
            key = { _, link -> link.id },
        ) { index, link ->
            RoutineTaskItem(
                index = index,
                itemCount = links.size,
                link = link,
                routine = routine,
                viewModel = viewModel,
                schedule = schedule,
                draggingIndex = draggingIndex,
                dragOffsetY = dragOffsetY,
                isCurrent = currentTaskIndex == index,
                isCompleted = currentTaskIndex != null && index < currentTaskIndex,
                onEditClick = {
                    navigator.navigateTo(
                        AppRoute.EditTask(
                            arguments = EditTaskArguments(
                                taskId = link.task.id,
                                source = EditTaskSource.Routine(
                                    routineId = routine.id,
                                    linkId = link.id,
                                    selectedSubDataId = link.subData?.id,
                                )
                            )
                        )
                    )
                },
            )
        }
        item { Spacer(Modifier.height(200.dp)) }
    }
}