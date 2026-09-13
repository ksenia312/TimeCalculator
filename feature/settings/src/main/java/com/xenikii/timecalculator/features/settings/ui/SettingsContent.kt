package com.xenikii.timecalculator.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.NotificationMode
import com.xenikii.timecalculator.domain.model.PremiumPeriodType
import com.xenikii.timecalculator.domain.model.PremiumSource
import com.xenikii.timecalculator.domain.model.PremiumStatus
import com.xenikii.timecalculator.features.settings.presentation.SettingsViewState
import com.xenikii.timecalculator.shared.components.AppButtonMedium
import com.xenikii.timecalculator.shared.components.AppListItem
import com.xenikii.timecalculator.shared.components.appListItemColors
import com.xenikii.timecalculator.shared.extensions.bottomIndent
import com.xenikii.timecalculator.shared.extensions.stringDateTime
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme
import kotlin.time.Instant

@Composable
fun SettingsContent(
    viewState: SettingsViewState,
    onLogoutClick: () -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onNotificationModeChange: (NotificationMode) -> Unit,
    onOpenSystemNotificationSettings: () -> Unit,
    onManagePremiumClick: () -> Unit,
    onRestorePurchasesClick: () -> Unit,
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
        PremiumSettingsItem(
            premiumStatus = viewState.premiumStatus,
            isRestoringPurchases = viewState.isRestoringPurchases,
            onManagePremiumClick = onManagePremiumClick,
            onRestorePurchasesClick = onRestorePurchasesClick,
        )

        Spacer(modifier = Modifier.height(12.dp))
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
            onModeChange = onNotificationModeChange,
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
    onModeChange: (NotificationMode) -> Unit,
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

    if (viewState.isNotificationsSwitchOn) {
        Spacer(modifier = Modifier.height(8.dp))
        NotificationModeOption(
            label = stringResource(R.string.settings_notifications_mode_start_and_end),
            selected = viewState.notificationMode == NotificationMode.START_AND_END,
            onClick = { onModeChange(NotificationMode.START_AND_END) },
            supporting = stringResource(R.string.settings_notifications_mode_start_and_end_supporting),
            icon = Icons.Outlined.NotificationsActive
        )
        Spacer(modifier = Modifier.height(4.dp))
        NotificationModeOption(
            label = stringResource(R.string.settings_notifications_mode_every_task),
            selected = viewState.notificationMode == NotificationMode.EVERY_TASK,
            onClick = { onModeChange(NotificationMode.EVERY_TASK) },
            supporting = stringResource(R.string.settings_notifications_mode_every_task_supporting),
            icon = Icons.Filled.NotificationsActive,
            locked = !viewState.premiumStatus.isActive,
        )
    }
}

@Composable
private fun NotificationModeOption(
    label: String,
    supporting: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    locked: Boolean = false,
) {
    Box(Modifier.padding(start = 32.dp)) {
        AppListItem(
            colors = appListItemColors(),
            modifier = Modifier.clickable(onClick = onClick),
            minHeight = 32.dp,
            isSelected = selected,
            headlineContent = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            supportingContent = {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalCustomColorScheme.current.label,
                )
            },
            trailingContent = {
                if (locked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(R.string.content_desc_premium_locked),
                        modifier = Modifier.padding(end = 12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    RadioButton(
                        selected = selected,
                        onClick = onClick,
                    )
                }
            },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    Modifier.size(24.dp),
                )
            }
        )
    }
}

@Composable
private fun PremiumSettingsItem(
    premiumStatus: PremiumStatus,
    isRestoringPurchases: Boolean,
    onManagePremiumClick: () -> Unit,
    onRestorePurchasesClick: () -> Unit,
) {
    val isPremium = premiumStatus.isActive
    // A grant has no underlying Play subscription to manage, so tapping it would only open an
    // empty/irrelevant Play Store screen - the tile stays informational-only for that source.
    val isManageable = !isPremium || premiumStatus.source == PremiumSource.PURCHASE

    val backgroundColor = if (isPremium) {
        LocalCustomColorScheme.current.accent
    } else {
        MaterialTheme.colorScheme.surface
    }
    val color = if (isPremium) {
        MaterialTheme.colorScheme.background
    } else {
        LocalCustomColorScheme.current.label
    }
    AppListItem(
        colors = appListItemColors().copy(
            containerColor = backgroundColor,
            headlineColor = color,
            leadingIconColor = color,
            supportingTextColor = color,
            trailingIconColor = color
        ),
        modifier = if (isManageable) Modifier.clickable(onClick = onManagePremiumClick) else Modifier,
        headlineContent = {
            Text(
                text = stringResource(R.string.settings_premium_label),
                style = MaterialTheme.typography.titleMedium,
                color = if (isPremium) {
                    MaterialTheme.colorScheme.background
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        },
        supportingContent = {
            Text(
                text = premiumStatusSupportingText(premiumStatus),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.diamond),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        },
        trailingContent = {
            if (isManageable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    Modifier.size(24.dp),
                )
            }
        },
    )

    if (!isPremium) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(Modifier.padding(start = 32.dp)) {
            AppListItem(
                modifier = Modifier
                    .clickable(
                        enabled = !isRestoringPurchases,
                        onClick = onRestorePurchasesClick
                    ),
                minHeight = 32.dp,
                headlineContent = {
                    Text(
                        text = if (isRestoringPurchases) {
                            stringResource(R.string.settings_premium_restore_in_progress)
                        } else {
                            stringResource(R.string.settings_premium_restore_action)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
            )
        }
    }
}

/**
 * Explains *how* premium is active - trial/renewing/cancelled-but-active/lifetime/gifted - rather
 * than just "Premium is active", using the same date formatter already used elsewhere in the app
 * (no new date-formatting code).
 */
@Composable
private fun premiumStatusSupportingText(status: PremiumStatus): String {
    if (!status.isActive) return stringResource(R.string.settings_premium_status_free)

    val context = LocalContext.current
    val expirationDate: Instant? = status.expirationDate

    return when {
        status.source == PremiumSource.GRANT -> {
            val grantedUntil = status.grantedUntil
            if (grantedUntil != null) {
                stringResource(R.string.settings_premium_status_grant_until, grantedUntil.stringDateTime(context))
            } else {
                stringResource(R.string.settings_premium_status_grant)
            }
        }

        expirationDate == null -> stringResource(R.string.settings_premium_status_lifetime)

        status.periodType == PremiumPeriodType.TRIAL ->
            stringResource(R.string.settings_premium_status_trial, expirationDate.stringDateTime(context))

        status.willRenew == false ->
            stringResource(R.string.settings_premium_status_expiring, expirationDate.stringDateTime(context))

        status.willRenew == true ->
            stringResource(R.string.settings_premium_status_renews, expirationDate.stringDateTime(context))

        else -> stringResource(R.string.settings_premium_status_active)
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

@Preview
@Composable
private fun SettingsContentPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onNotificationModeChange = {},
            onOpenSystemNotificationSettings = {},
            onManagePremiumClick = {},
            onRestorePurchasesClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

@Preview
@Composable
private fun SettingsContentNotificationsBlockedPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(areSystemNotificationsAllowed = false),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onNotificationModeChange = {},
            onOpenSystemNotificationSettings = {},
            onManagePremiumClick = {},
            onRestorePurchasesClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

@Preview
@Composable
private fun SettingsContentLoggingOutPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(
                isLoggingOut = true,
                notificationsEnabled = true,
                notificationMode = NotificationMode.START_AND_END
            ),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onNotificationModeChange = {},
            onOpenSystemNotificationSettings = {},
            onManagePremiumClick = {},
            onRestorePurchasesClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

@Preview
@Composable
private fun SettingsContentPremiumRenewingPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(
                premiumStatus = PremiumStatus(
                    isActive = true,
                    source = PremiumSource.PURCHASE,
                    expirationDate = PreviewFutureDate,
                    willRenew = true,
                    periodType = PremiumPeriodType.NORMAL,
                ),
                notificationsEnabled = true,
                notificationMode = NotificationMode.EVERY_TASK
            ),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onNotificationModeChange = {},
            onOpenSystemNotificationSettings = {},
            onManagePremiumClick = {},
            onRestorePurchasesClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

@Preview
@Composable
private fun SettingsContentPremiumTrialPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(
                premiumStatus = PremiumStatus(
                    isActive = true,
                    source = PremiumSource.PURCHASE,
                    expirationDate = PreviewFutureDate,
                    willRenew = true,
                    periodType = PremiumPeriodType.TRIAL,
                ),
            ),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onNotificationModeChange = {},
            onOpenSystemNotificationSettings = {},
            onManagePremiumClick = {},
            onRestorePurchasesClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

@Preview
@Composable
private fun SettingsContentPremiumCancelledPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(
                premiumStatus = PremiumStatus(
                    isActive = true,
                    source = PremiumSource.PURCHASE,
                    expirationDate = PreviewFutureDate,
                    willRenew = false,
                    periodType = PremiumPeriodType.NORMAL,
                ),
            ),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onNotificationModeChange = {},
            onOpenSystemNotificationSettings = {},
            onManagePremiumClick = {},
            onRestorePurchasesClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

@Preview
@Composable
private fun SettingsContentPremiumLifetimePreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(
                premiumStatus = PremiumStatus(
                    isActive = true,
                    source = PremiumSource.PURCHASE,
                    expirationDate = null,
                    willRenew = true,
                ),
            ),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onNotificationModeChange = {},
            onOpenSystemNotificationSettings = {},
            onManagePremiumClick = {},
            onRestorePurchasesClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

@Preview
@Composable
private fun SettingsContentPremiumGrantPreview() {
    PreviewTheme {
        SettingsContent(
            viewState = SettingsViewState(
                premiumStatus = PremiumStatus(
                    isActive = true,
                    source = PremiumSource.GRANT,
                    grantedUntil = PreviewFutureDate,
                    grantReason = "Founder",
                ),
            ),
            onLogoutClick = {},
            onNotificationsEnabledChange = {},
            onNotificationModeChange = {},
            onOpenSystemNotificationSettings = {},
            onManagePremiumClick = {},
            onRestorePurchasesClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

private val PreviewFutureDate = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)