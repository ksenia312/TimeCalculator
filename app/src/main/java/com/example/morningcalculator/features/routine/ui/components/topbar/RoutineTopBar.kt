package com.example.morningcalculator.features.routine.ui.components.topbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.morningcalculator.R
import com.example.morningcalculator.features.home.ui.components.RoutineDialog
import com.example.morningcalculator.features.home.ui.components.RoutineDialogViewState
import com.example.morningcalculator.features.routine.presentation.RoutineViewState

@Composable
fun RoutineTopBar(
    viewState: RoutineViewState,
    onShowEditDialog: () -> Unit,
    onRoutineDialogViewStateChange: (RoutineDialogViewState) -> Unit,
    onRoutineDialogConfirm: () -> Unit,
    onRoutineDialogDismiss: () -> Unit,
    onRoutineDelete: () -> Unit,
) {
    when (viewState) {
        is RoutineViewState.Success -> {
            val routineDialogViewState = viewState.routineDialogViewState
            if (routineDialogViewState?.isVisible == true) {
                RoutineDialog(
                    screenTitle = stringResource(R.string.routine_dialog_edit_title),
                    viewState = routineDialogViewState,
                    onStateChange = onRoutineDialogViewStateChange,
                    onConfirm = onRoutineDialogConfirm,
                    onDismiss = onRoutineDialogDismiss,
                    onDelete = onRoutineDelete,
                )
            }
            RoutineSuccessTopAppBar(
                viewState,
                onShowEditDialog = onShowEditDialog
            )
        }

        is RoutineViewState.Error -> NonSuccessTopAppBar(stringResource(R.string.top_bar_error))
        is RoutineViewState.Loading -> NonSuccessTopAppBar(stringResource(R.string.top_bar_loading))
    }
}