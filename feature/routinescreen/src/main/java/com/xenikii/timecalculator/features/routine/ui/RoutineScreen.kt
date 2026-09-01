package com.xenikii.timecalculator.features.routine.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.routine.presentation.RoutineViewModel
import com.xenikii.timecalculator.features.routine.presentation.RoutineViewState
import com.xenikii.timecalculator.features.routine.ui.components.EditRoutineFloatingButton
import com.xenikii.timecalculator.features.routine.ui.components.RoutineColorWrapper
import com.xenikii.timecalculator.features.routine.ui.components.TasksListView
import com.xenikii.timecalculator.features.routine.ui.components.topbar.RoutineTopBar
import com.xenikii.timecalculator.features.routine.ui.components.topbar.rememberCollapsingTopBarState
import com.xenikii.timecalculator.shared.components.AppScaffold
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    id: String,
    viewModel: RoutineViewModel = koinViewModel(
        parameters = { parametersOf(id) }
    ),
) {
    val navigator = LocalNavigator.current
    val viewState by viewModel.viewState.collectAsState()
    val collapsingTopBar = rememberCollapsingTopBarState()

    RoutineColorWrapper(viewState) {
        AppScaffold(
            topBar = {
                RoutineTopBar(
                    viewState = viewState,
                    collapseFraction = collapsingTopBar.fraction,
                    scrollableState = collapsingTopBar.scrollableState,
                    onShowEditDialog = {
                        navigator.navigateTo(
                            AppRoute.EditRoutine(
                                routineId = id,
                                fromRoutineScreen = true,
                            )
                        )
                    },
                )
            },
            floatingActionButton = {
                val routine = (viewState as? RoutineViewState.Success)?.routine
                if (routine != null) EditRoutineFloatingButton(routine, viewModel)
            }) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .nestedScroll(collapsingTopBar.nestedScrollConnection),
            ) {
                when (val viewState = viewState) {
                    is RoutineViewState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is RoutineViewState.Success -> {
                        val combined = viewState.routine
                        TasksListView(
                            routine = combined,
                            schedule = viewState.schedule,
                            viewModel = viewModel,
                            currentTaskIndex = viewState.currentTaskIndex,
                        )
                    }

                    is RoutineViewState.Error -> {
                        Text(text = stringResource(R.string.top_bar_error))
                    }
                }
            }
        }
    }
}