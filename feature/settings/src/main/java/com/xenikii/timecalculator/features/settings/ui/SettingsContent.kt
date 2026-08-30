package com.xenikii.timecalculator.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.settings.presentation.SettingsViewState
import com.xenikii.timecalculator.shared.components.AppButtonMedium
import com.xenikii.timecalculator.shared.components.AppListItem
import com.xenikii.timecalculator.shared.extensions.bottomIndent
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme

@Composable
fun SettingsContent(
    viewState: SettingsViewState,
    onLogoutClick: () -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onOpenSystemNotificationSettings: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val email = viewState.user?.email ?: stringResource(R.string.settings_email_placeholder)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
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

        Spacer(modifier = Modifier.height(12.dp))

        NotificationSettingsItem(
            viewState = viewState,
            onEnabledChange = onNotificationsEnabledChange,
            onOpenSystemNotificationSettings = onOpenSystemNotificationSettings,
        )

        Spacer(modifier = Modifier.height(12.dp))

        LinkSettingsItem(
            label = stringResource(R.string.settings_privacy_policy_label),
            supporting = stringResource(R.string.settings_privacy_policy_supporting),
            icon = Icons.Filled.PrivacyTip,
            onClick = onPrivacyPolicyClick,
        )

        Spacer(modifier = Modifier.height(12.dp))

        LinkSettingsItem(
            label = stringResource(R.string.settings_terms_label),
            supporting = stringResource(R.string.settings_terms_supporting),
            icon = Icons.AutoMirrored.Filled.Article,
            onClick = onTermsClick,
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppButtonMedium(
            onClick = onLogoutClick,
            enabled = !viewState.isLoggingOut,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
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
                    color = ButtonDefaults.buttonColors().disabledContentColor,
                    strokeWidth = 2.dp,
                )
                Text(
                    text = stringResource(R.string.settings_action_logging_out),
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_action_logout),
                )
            }
        }
        Spacer(
            modifier = Modifier.bottomIndent()
        )
    }
}

@Composable
private fun ColumnScope.NotificationSettingsItem(
    viewState: SettingsViewState,
    onEnabledChange: (Boolean) -> Unit,
    onOpenSystemNotificationSettings: () -> Unit,
) {
    val allowed = viewState.areSystemNotificationsAllowed

    AppListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.settings_notifications_label),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(
                text = if (allowed) {
                    stringResource(R.string.settings_notifications_supporting)
                } else {
                    stringResource(R.string.settings_notifications_blocked)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = LocalCustomColorScheme.current.label,
            )
        },
        leadingContent = {
            Icon(
                imageVector = if (viewState.isNotificationsSwitchOn) {
                    Icons.Filled.Notifications
                } else {
                    Icons.Filled.NotificationsOff
                },
                contentDescription = null,
                Modifier.size(24.dp),
            )
        },
        trailingContent = {
            Switch(
                checked = viewState.isNotificationsSwitchOn,
                onCheckedChange = onEnabledChange,
                enabled = allowed,
            )
        },
    )

    if (!allowed) {
        Spacer(modifier = Modifier.height(12.dp))
        AppButtonMedium(
            onClick = onOpenSystemNotificationSettings,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.settings_notifications_open_system_settings),
            )
        }
    }
}

@Composable
private fun LinkSettingsItem(
    label: String,
    supporting: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    AppListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalCustomColorScheme.current.label,
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                Modifier.size(24.dp),
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                Modifier.size(24.dp),
            )
        },
    )
}

@PreviewAll
@Composable
private fun SettingsContentPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onOpenSystemNotificationSettings = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

@PreviewAll
@Composable
private fun SettingsContentNotificationsBlockedPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(areSystemNotificationsAllowed = false),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onOpenSystemNotificationSettings = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
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
            onNotificationsEnabledChange = {},
            onOpenSystemNotificationSettings = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}
