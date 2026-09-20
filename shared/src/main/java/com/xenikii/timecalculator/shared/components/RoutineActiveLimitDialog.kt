package com.xenikii.timecalculator.shared.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.repository.FREE_ROUTINE_LIMIT
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator

/**
 * Shown when activating a paused routine is blocked by the free-tier limit (see
 * `ActivateRoutineUseCase`). Shared by `routineslist` and `routineeditor` so the copy and the
 * paywall hand-off aren't duplicated across feature modules.
 */
@Composable
fun RoutineActiveLimitDialog(onDismiss: () -> Unit) {
    val navigator = LocalNavigator.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.routine_active_limit_dialog_title)) },
        text = { Text(stringResource(R.string.routine_active_limit_dialog_message, FREE_ROUTINE_LIMIT)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                navigator.navigateTo(AppRoute.Paywall)
            }) {
                Text(stringResource(R.string.routine_limit_resolution_buy_premium_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
