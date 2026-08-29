package com.xenikii.timecalculator.features.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.settings.presentation.SettingsViewState
import com.xenikii.timecalculator.shared.components.AppButton
import com.xenikii.timecalculator.shared.components.AppListItem
import com.xenikii.timecalculator.shared.components.DeleteConfirmationDialog
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme

@Composable
fun SettingsContent(
    viewState: SettingsViewState,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val email = viewState.user?.email ?: stringResource(R.string.settings_email_placeholder)
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.settings_logout_dialog_title),
            message = stringResource(R.string.settings_logout_dialog_message),
            confirmText = stringResource(R.string.settings_logout_dialog_confirm),
            onConfirm = onLogoutClick,
            onDismiss = { showLogoutDialog = false },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        AppListItem(
            headlineContent = {
                Text(
                    text = stringResource(R.string.settings_email_label),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            supportingContent = {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalCustomColorScheme.current.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.AlternateEmail,
                    contentDescription = null,
                    Modifier.size(24.dp),
                )
            },
        )

        Spacer(modifier = Modifier.height(20.dp))

        AppButton(
            onClick = { showLogoutDialog = true },
            enabled = !viewState.isLoggingOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            if (viewState.isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(16.dp),
                    color = MaterialTheme.colorScheme.onError,
                    strokeWidth = 2.dp,
                )
                Text(
                    text = stringResource(R.string.settings_action_logging_out),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onError
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_action_logout),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onError
                    )
                )
            }
        }
    }
}

@PreviewAll
@Composable
private fun SettingsContentPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(),
            onLogoutClick = {},
        )
    }
}

@PreviewAll
@Composable
private fun SettingsContentLoggingOutPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(isLoggingOut = true),
            onLogoutClick = {},
        )
    }
}