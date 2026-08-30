package com.xenikii.timecalculator.features.routine.ui.components.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.components.BackButton

/**
 * Compact header shown while the routine top bar is collapsed: the back button with the
 * routine title placed to its right (instead of the large card below it), and a settings
 * action pinned to the top-right corner.
 *
 * The title is only laid out once it starts appearing ([collapsedTitleAlpha] > 0), and its
 * accessibility text is cleared unless it is the currently visible title ([ownsTitleForA11y]),
 * so screen readers never announce a duplicated or hidden title.
 */
@Composable
fun RoutineCollapsingHeader(
    title: String,
    collapsedTitleAlpha: Float,
    ownsTitleForA11y: Boolean,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(color = MaterialTheme.colorScheme.onPrimary)

        Box(modifier = Modifier.weight(1f)) {
            if (collapsedTitleAlpha > 0f) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.surface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = collapsedTitleAlpha
                            translationY = (1f - collapsedTitleAlpha) * 10.dp.toPx()
                        }
                        .padding(end = 16.dp)
                        .then(
                            if (ownsTitleForA11y) Modifier else Modifier.clearAndSetSemantics { }
                        ),
                )
            }
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.content_desc_routine_settings),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
