package com.example.morningcalculator.features.routine.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.morningcalculator.shared.navigator.LocalNavigator
import org.koin.compose.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    id: String,
    viewModel: RoutineViewModel? = null,
) {
    val koin = getKoin()
    val navigator = LocalNavigator.current
    val resolvedViewModel = viewModel ?: remember(id) {
        RoutineViewModel(
            id = id,
            tasksRepository = koin.get(),
            routineRepository = koin.get()
        )
    }
    val viewState by resolvedViewModel.viewState.collectAsState()
    RoutineColorWrapper(viewState) {
        AppScaffold(
            topBar = {
                RoutineTopBar(
                    viewState = viewState,
                    onShowEditDialog = resolvedViewModel::onShowEditDialog,
                    onRoutineDialogViewStateChange = resolvedViewModel::onRoutineDialogViewStateChange,
                    onRoutineDialogConfirm = resolvedViewModel::onRoutineDialogConfirm,
                    onRoutineDialogDismiss = resolvedViewModel::onRoutineDialogDismiss,
                    onRoutineDelete = {
                        resolvedViewModel.deleteRoutine()
                        navigator.navigateBack()
                    },
                )
            },
            floatingActionButton = {
                val routine = (viewState as? RoutineViewState.Success)?.routine
                if (routine != null) EditRoutineFloatingButton(routine, resolvedViewModel)
            }) {

            Box(modifier = Modifier.padding(it)) {
                when (val viewState = viewState) {
                    is RoutineViewState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is RoutineViewState.Success -> {
                        val combined = viewState.routine
                        TasksListView(combined, resolvedViewModel)
                    }

                    is RoutineViewState.Error -> {
                        Text(text = stringResource(R.string.top_bar_error))
                    }
                }
            }
        }
    }
}