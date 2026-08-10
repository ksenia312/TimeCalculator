package com.xenikii.timecalculator.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val appListItemShape = RoundedCornerShape(24.dp)

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
    ListItem(
        modifier = modifier.appListItemContainer(
            isSelected = isSelected,
            minHeight = minHeight,
        ),
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = colors,
    ) {
        headlineContent()
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
