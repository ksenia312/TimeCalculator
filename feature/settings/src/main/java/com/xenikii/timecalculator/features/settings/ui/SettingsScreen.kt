package com.xenikii.timecalculator.features.settings.ui

import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
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
import com.xenikii.timecalculator.features.settings.presentation.SettingsViewModel
import com.xenikii.timecalculator.shared.components.DeleteConfirmationDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val viewState = viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val privacyPolicyUrl = stringResource(R.string.settings_privacy_policy_url)
    val termsUrl = stringResource(R.string.settings_terms_url)
    var showLogoutDialog by remember { mutableStateOf(false) }
    val urlHandler = LocalUriHandler.current

    LifecycleResumeEffect(Unit) {
        viewModel.refreshNotificationPermission()
        onPauseOrDispose { }
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
        onTermsClick = {
            runCatching { urlHandler.openUri(termsUrl) }
                .onFailure {
                    Log.e("SettingsScreen", "Failed to open terms URL: $termsUrl", it)
                }
        },
    )
}
