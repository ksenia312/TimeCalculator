package com.example.morningcalculator.features.routine.ui.components.topbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.morningcalculator.core.mapper.copyWithRequest
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.home.ui.components.RoutineDialog
import com.example.morningcalculator.features.routine.presentation.RoutineViewModel
import com.example.morningcalculator.features.routine.presentation.RoutineViewState

@Composable
fun RoutineTopBar(viewModel: RoutineViewModel) {
    val viewState by viewModel.viewState.collectAsState()
    val editingRoutine = remember { mutableStateOf<Routine?>(null) }

    if (editingRoutine.value != null) {
        val routine = editingRoutine.value!!
        RoutineDialog(
            initialRoutine = routine,
            onConfirm = { request ->
                viewModel.editRoutine(
                    routine.copyWithRequest(request)
                )
                editingRoutine.value = null
            },
            onDismiss = {
                editingRoutine.value = null
            }
        )
    }

    when (val state = viewState) {
        is RoutineViewState.Success -> {
            SuccessTopAppBar(
                state,
                onShowEditDialog = { editingRoutine.value = state.full }
            )
        }

        is RoutineViewState.Error -> NonSuccessTopAppBar("Error")
        is RoutineViewState.Loading -> NonSuccessTopAppBar("Loading")
    }
}