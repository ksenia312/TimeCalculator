package com.xenikii.timecalculator.features.settings.ui

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.PremiumSource
import com.xenikii.timecalculator.features.settings.presentation.RestoreResult
import com.xenikii.timecalculator.features.settings.presentation.SettingsViewModel
import com.xenikii.timecalculator.shared.components.DeleteConfirmationDialog
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current
    val viewState = viewModel.viewState.collectAsStateWithLifecycle()
    val showPaywall by viewModel.showPaywall.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val privacyPolicyUrl = stringResource(R.string.settings_privacy_policy_url)
    val termsUrl = stringResource(R.string.settings_terms_url)
    var showLogoutDialog by remember { mutableStateOf(false) }
    val urlHandler = LocalUriHandler.current
    val restoredMessage = stringResource(R.string.settings_premium_restore_success)
    val nothingToRestoreMessage = stringResource(R.string.settings_premium_restore_empty)

    LifecycleResumeEffect(Unit) {
        viewModel.refreshNotificationPermission()
        onPauseOrDispose { }
    }

    LaunchedEffect(showPaywall) {
        if (showPaywall) {
            navigator.navigateTo(AppRoute.Paywall)
            viewModel.onPaywallShown()
        }
    }

    LaunchedEffect(viewState.value.restoreResult) {
        when (viewState.value.restoreResult) {
            RestoreResult.Restored -> Toast.makeText(context, restoredMessage, Toast.LENGTH_SHORT).show()
            RestoreResult.NothingToRestore -> Toast.makeText(context, nothingToRestoreMessage, Toast.LENGTH_SHORT).show()
            null -> Unit
        }
        if (viewState.value.restoreResult != null) {
            viewModel.onRestoreResultShown()
        }
    }

    if (showLogoutDialog) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.settings_logout_dialog_title),
            message = stringResource(R.string.settings_logout_dialog_message),
            confirmText = stringResource(R.string.settings_logout_dialog_confirm),
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onDismiss = { showLogoutDialog = false },
        )
    }

    SettingsContent(
        viewState = viewState.value,
        onLogoutClick = { showLogoutDialog = true },
        onNotificationsEnabledChange = viewModel::setNotificationsEnabled,
        onNotificationModeChange = viewModel::setNotificationMode,
        onOpenSystemNotificationSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                },
            )
        },
        onPrivacyPolicyClick = {
            runCatching { urlHandler.openUri(privacyPolicyUrl) }
                .onFailure {
                    Log.e("SettingsScreen", "Failed to open privacy policy URL: $privacyPolicyUrl", it)
                }
        },
        onManagePremiumClick = {
            val premiumStatus = viewState.value.premiumStatus
            when {
                !premiumStatus.isActive -> navigator.navigateTo(AppRoute.Paywall)
                premiumStatus.source == PremiumSource.PURCHASE -> navigator.navigateTo(AppRoute.ManageSubscription)
                // Granted (gift/founder/promo) premium has no Play subscription to manage; the
                // tile is non-clickable for this case (see PremiumSettingsItem), so this is unreachable.
                else -> Unit
            }
        },
        onRestorePurchasesClick = viewModel::restorePurchases,
        onTermsClick = {
            runCatching { urlHandler.openUri(termsUrl) }
                .onFailure {
                    Log.e("SettingsScreen", "Failed to open terms URL: $termsUrl", it)
                }
        },
    )
}
