package com.example.morningcalculator.features.routine.ui.views.top_bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.home.ui.views.RoutineDialog
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import com.example.morningcalculator.features.routine.view_model.RoutineViewState

@Composable
fun RoutineTopBar(
    viewModel: RoutineViewModel
) {
    val viewState by viewModel.viewState.collectAsState()
    val editingRoutine = remember { mutableStateOf<Routine.Links?>(null) }
    if (editingRoutine.value != null) {
        val routine = editingRoutine.value!!
        RoutineDialog(initialRoutine = routine, onConfirm = { request ->
            viewModel.editRoutine(
                routine.copy(
                    title = request.title, time = request.time
                )
            )
            editingRoutine.value = null
        }, onDismiss = {
            editingRoutine.value = null
        })
    }

    when (val viewState = viewState) {
        is RoutineViewState.Success -> {
            SuccessTopAppBar(viewState, editingRoutine)
        }

        is RoutineViewState.Error -> NonSuccessTopAppBar("Error")
        is RoutineViewState.Loading -> NonSuccessTopAppBar("Loading")
    }
}