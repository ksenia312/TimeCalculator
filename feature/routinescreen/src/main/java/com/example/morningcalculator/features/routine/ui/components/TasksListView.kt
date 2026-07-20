package com.example.morningcalculator.features.routine.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.routine.presentation.RoutineViewModel
import com.example.morningcalculator.shared.extensions.stringTime
import com.example.morningcalculator.shared.extensions.whenToStart
import com.example.morningcalculator.shared.navigator.AppRoute
import com.example.morningcalculator.shared.navigator.EditTaskArguments
import com.example.morningcalculator.shared.navigator.EditTaskSource
import com.example.morningcalculator.shared.navigator.LocalNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListView(
    routine: Routine,
    viewModel: RoutineViewModel,
    currentTaskIndex: Int?,
) {
    val navigator = LocalNavigator.current

    val whenToGetUp = routine.whenToStart().stringTime()
    val fullLinks = remember(routine) { routine.data.toMutableStateList() }
    val draggingIndex = remember { mutableStateOf<Int?>(null) }
    val dragOffsetY = remember { mutableFloatStateOf(0f) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item { Spacer(Modifier.height(12.dp)) }
        if (fullLinks.isNotEmpty()) {
            item(key = "wakeUp") {
                TimeSegment(
                    whenToGetUp,
                    isTitle = true,
                    useSeparator = false,
                )
            }
        }
        itemsIndexed(
            items = fullLinks,
            key = { _, link -> link.id },
        ) { index, link ->
            RoutineTaskItem(
                routineLinks = fullLinks,
                index = index,
                linkFull = link,
                routine = routine,
                viewModel = viewModel,
                draggingIndex = draggingIndex,
                dragOffsetY = dragOffsetY,
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
                isCurrent = currentTaskIndex == index,
            )
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}