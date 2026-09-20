package com.xenikii.timecalculator.features.routinelimitresolution.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.repository.FREE_ROUTINE_LIMIT
import com.xenikii.timecalculator.features.routinelimitresolution.presentation.RoutineLimitResolutionViewState
import com.xenikii.timecalculator.features.routinelimitresolution.presentation.SaveState
import com.xenikii.timecalculator.features.routinelimitresolution.ui.components.RoutineLimitResolutionItemRow
import com.xenikii.timecalculator.shared.components.AppButtonExpressive
import com.xenikii.timecalculator.shared.components.AppButtonMedium
import com.xenikii.timecalculator.shared.components.AppTextButtonMedium
import com.xenikii.timecalculator.shared.extensions.bottomIndent
import com.xenikii.timecalculator.shared.features.EditorScreenScaffold
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme

@Composable
fun RoutineLimitResolutionContent(
    viewState: RoutineLimitResolutionViewState,
    isRestoring: Boolean,
    onToggleSelect: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBuyPremiumClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    EditorScreenScaffold(
        screenTitle = stringResource(R.string.routine_limit_resolution_title),
        onDismiss = onDismiss,
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.routine_limit_resolution_subtitle, FREE_ROUTINE_LIMIT),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(Modifier.height(12.dp))

                when (viewState) {
                    RoutineLimitResolutionViewState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is RoutineLimitResolutionViewState.Content -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            viewState.items.forEach { item ->
                                key(item.id) {
                                    RoutineLimitResolutionItemRow(
                                        item = item,
                                        onToggleSelect = { onToggleSelect(item.id) },
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Spacer(Modifier.weight(1f))

                        Text(
                            text = stringResource(
                                R.string.routine_limit_resolution_selected_count,
                                viewState.selectedCount,
                                FREE_ROUTINE_LIMIT,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                        )

                        if (viewState.saveState.failed) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.routine_limit_resolution_save_error),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        AppButtonMedium(
                            enabled = viewState.canSave,
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth(),
                            onClick = onSaveClick,
                        ) {
                            Text(stringResource(R.string.action_save))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                AppButtonMedium(
                    onClick = onBuyPremiumClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalCustomColorScheme.current.accent,
                    ),
                ) {
                    Text(stringResource(R.string.routine_limit_resolution_buy_premium_action))
                }

                Spacer(Modifier.height(8.dp))

                AppTextButtonMedium(
                    onClick = onRestoreClick,
                    enabled = !isRestoring,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(
                            if (isRestoring) {
                                R.string.settings_premium_restore_in_progress
                            } else {
                                R.string.settings_premium_restore_action
                            }
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))
                Box(Modifier.bottomIndent())
            }
        }
    }
}

@PreviewAll
@Composable
private fun RoutineLimitResolutionContentPreview() {
    PreviewTheme {
        RoutineLimitResolutionContent(
            viewState = RoutineLimitResolutionViewState.Content(
                items = emptyList(),
                selectedCount = 3,
                canSave = true,
                saveState = SaveState(),
            ),
            isRestoring = false,
            onToggleSelect = {},
            onSaveClick = {},
            onBuyPremiumClick = {},
            onRestoreClick = {},
            onDismiss = {},
        )
    }
}
