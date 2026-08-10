package com.xenikii.timecalculator.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val appListItemShape = RoundedCornerShape(24.dp)
private val appListItemHorizontalPadding = 16.dp
private val appListItemVerticalPadding = 8.dp
private val appListItemThreeLineVerticalPadding = 12.dp
private val appListItemLeadingSpacing = 16.dp
private val appListItemTrailingSpacing = 16.dp

@Composable
fun AppListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    minHeight: Dp = 72.dp,
    overlineContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    colors: ListItemColors = appListItemColors(),
) {
    val verticalPadding = if (overlineContent != null && supportingContent != null) {
        appListItemThreeLineVerticalPadding
    } else {
        appListItemVerticalPadding
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .appListItemContainer(
                isSelected = isSelected,
                minHeight = minHeight,
            )
            .then(modifier)
            .padding(
                horizontal = appListItemHorizontalPadding,
                vertical = verticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.let {
            Box(
                modifier = Modifier.padding(end = appListItemLeadingSpacing),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.leadingContentColor,
                    content = it,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
        ) {
            overlineContent?.let {
                CompositionLocalProvider(
                    LocalContentColor provides colors.overlineContentColor,
                    LocalTextStyle provides MaterialTheme.typography.labelSmall,
                    content = it,
                )
            }

            CompositionLocalProvider(
                LocalContentColor provides colors.contentColor,
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                content = headlineContent,
            )

            supportingContent?.let {
                CompositionLocalProvider(
                    LocalContentColor provides colors.supportingContentColor,
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    content = it,
                )
            }
        }

        trailingContent?.let {
            Box(
                modifier = Modifier
                    .padding(start = appListItemTrailingSpacing)
                    .widthIn(min = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.trailingContentColor,
                    LocalTextStyle provides MaterialTheme.typography.labelSmall,
                    content = it,
                )
            }
        }
    }
}

@Composable
private fun Modifier.appListItemContainer(
    isSelected: Boolean = false,
    minHeight: Dp = 72.dp,
): Modifier {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    return this
        .heightIn(min = minHeight)
        .clip(appListItemShape)
        .background(backgroundColor)
}

@Composable
private fun appListItemColors(): ListItemColors = ListItemDefaults.colors(
    containerColor = Color.Transparent,
)
