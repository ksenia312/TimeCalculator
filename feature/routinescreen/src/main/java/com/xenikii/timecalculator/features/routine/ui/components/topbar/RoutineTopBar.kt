package com.xenikii.timecalculator.features.routine.ui.components.topbar

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.routine.presentation.RoutineViewState

@Composable
fun RoutineTopBar(
    viewState: RoutineViewState,
    collapseFraction: Float = 0f,
    scrollableState: ScrollableState? = null,
    onShowEditDialog: () -> Unit,
) {
    when (viewState) {
        is RoutineViewState.Success -> {
            RoutineSuccessTopAppBar(
                viewState,
                collapseFraction = collapseFraction,
                scrollableState = scrollableState,
                onShowEditDialog = onShowEditDialog
            )
        }

        is RoutineViewState.Error -> NonSuccessTopAppBar(stringResource(R.string.top_bar_error))
        is RoutineViewState.Loading -> NonSuccessTopAppBar()
    }
}