package com.xenikii.timecalculator.features.routinelimitresolution.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.routinelimitresolution.presentation.RestoreResult
import com.xenikii.timecalculator.features.routinelimitresolution.presentation.RoutineLimitResolutionViewModel
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel

@Composable
fun RoutineLimitResolutionScreen(
    viewModel: RoutineLimitResolutionViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current

    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()
    val restoreResult by viewModel.restoreResult.collectAsStateWithLifecycle()

    val restoredMessage = stringResource(R.string.settings_premium_restore_success)
    val nothingToRestoreMessage = stringResource(R.string.settings_premium_restore_empty)
    LaunchedEffect(restoreResult) {
        when (restoreResult) {
            RestoreResult.RESTORED -> Toast.makeText(context, restoredMessage, Toast.LENGTH_SHORT).show()
            RestoreResult.NOTHING_TO_RESTORE -> Toast.makeText(context, nothingToRestoreMessage, Toast.LENGTH_SHORT).show()
            null -> Unit
        }
        if (restoreResult != null) viewModel.onRestoreResultShown()
    }

    RoutineLimitResolutionContent(
        viewState = viewState,
        isRestoring = isRestoring,
        onToggleSelect = viewModel::toggleSelection,
        onSaveClick = viewModel::save,
        onBuyPremiumClick = { navigator.navigateTo(AppRoute.Paywall) },
        onRestoreClick = viewModel::restorePurchases,
        onDismiss = navigator::navigateBack,
    )
}
