package com.xenikii.timecalculator.features.routinelimitresolution.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.routinelimitresolution.presentation.RoutineLimitResolutionItem
import com.xenikii.timecalculator.shared.components.AppListItem
import com.xenikii.timecalculator.shared.extensions.stringDateTime
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import kotlin.time.Instant

@Composable
fun RoutineLimitResolutionItemRow(
    item: RoutineLimitResolutionItem,
    onToggleSelect: () -> Unit,
) {
    val context = LocalContext.current

    AppListItem(
        modifier = Modifier.clickable(onClick = onToggleSelect),
        isSelected = item.isSelected,
        leadingContent = {
            Icon(
                imageVector = if (item.isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        headlineContent = {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
        },
        supportingContent = {
            val lastTriggeredAt = item.lastTriggeredAt
            val text = if (lastTriggeredAt != null) {
                stringResource(
                    R.string.routine_limit_resolution_last_triggered,
                    Instant.fromEpochMilliseconds(lastTriggeredAt).stringDateTime(context),
                )
            } else {
                stringResource(R.string.routine_limit_resolution_never_triggered)
            }
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        },
    )
}

@PreviewAll
@Composable
private fun RoutineLimitResolutionItemRowPreview() {
    PreviewTheme {
        RoutineLimitResolutionItemRow(
            item = RoutineLimitResolutionItem(
                id = "1",
                title = "Morning routine",
                lastTriggeredAt = System.currentTimeMillis(),
                isSelected = true,
            ),
            onToggleSelect = {},
        )
    }
}
