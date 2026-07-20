package com.example.morningcalculator.features.routine.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.morningcalculator.R
import com.example.morningcalculator.features.routine.presentation.RoutineViewModel
import com.example.morningcalculator.features.routine.presentation.RoutineViewState
import com.example.morningcalculator.features.routine.ui.components.EditRoutineFloatingButton
import com.example.morningcalculator.features.routine.ui.components.RoutineColorWrapper
import com.example.morningcalculator.features.routine.ui.components.TasksListView
import com.example.morningcalculator.features.routine.ui.components.topbar.RoutineTopBar
import com.example.morningcalculator.shared.components.AppScaffold
import com.example.morningcalculator.shared.navigator.AppRoute
import com.example.morningcalculator.shared.navigator.LocalNavigator
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
    RoutineColorWrapper(viewState) {
        AppScaffold(
            topBar = {
                RoutineTopBar(
                    viewState = viewState,
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

            Box(modifier = Modifier.padding(it)) {
                when (val viewState = viewState) {
                    is RoutineViewState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is RoutineViewState.Success -> {
                        val combined = viewState.routine
                        TasksListView(
                            routine = combined,
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