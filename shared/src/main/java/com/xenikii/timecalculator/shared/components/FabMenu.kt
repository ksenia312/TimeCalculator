package com.xenikii.timecalculator.shared.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingActionButtonMenuScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.theme.TimeCalculatorTheme

data class FabItem(
    val iconRes: Int,
    val title: String,
    val contentDescription: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onChangeExpanded: (Boolean) -> Unit,
    fabItems: List<FabItem>,
    mainContainerColor: Color = MaterialTheme.colorScheme.primary,
    mainContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    itemContainerColor: Color = MaterialTheme.colorScheme.primary,
    itemContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    mainImageVector: ImageVector = Icons.Default.Add,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
) {
    val rotation: Float by animateFloatAsState(if (isExpanded) 180f else 0f)
    val colors = ButtonDefaults.elevatedButtonColors(
        containerColor = itemContainerColor, contentColor = itemContentColor
    )
    val largeContainerSize = ToggleFloatingActionButtonDefaults.containerSizeLarge()(0f)

    BackHandler(isExpanded) { onChangeExpanded(false) }
    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = isExpanded,
        horizontalAlignment = horizontalAlignment,
        button = {
            ToggleFloatingActionButton(
                checked = isExpanded,
                onCheckedChange = onChangeExpanded,
                containerColor = { mainContainerColor },
                containerSize = ToggleFloatingActionButtonDefaults.containerSize(
                    initialSize = largeContainerSize,
                    finalSize = largeContainerSize
                ),
                containerCornerRadius = ToggleFloatingActionButtonDefaults.containerCornerRadiusLarge(),
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else mainImageVector,
                    contentDescription = if (isExpanded) {
                        stringResource(R.string.content_desc_close_menu)
                    } else {
                        stringResource(R.string.content_desc_open_menu)
                    },
                    tint = mainContentColor,
                    modifier = Modifier
                        .size(FloatingActionButtonDefaults.LargeIconSize)
                        .rotate(rotation)
                )
            }
        }
    ) {
        fabItems.forEachIndexed { index, item ->
            ElevatedButtonWithIconM3(
                iconRes = item.iconRes,
                colors = colors,
                onClick = {
                    item.onClick()
                    onChangeExpanded(false)
                },
                text = item.title,
                contentDescription = item.contentDescription,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingActionButtonMenuScope.ElevatedButtonWithIconM3(
    onClick: () -> Unit,
    text: String,
    iconRes: Int,
    contentDescription: String,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors()
) {
    FloatingActionButtonMenuItem(
        onClick = onClick,
        containerColor = colors.containerColor,
        contentColor = colors.contentColor,
        icon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription
            )
        },
        text = {
            Text(text)
        }
    )
}

@Preview
@Composable
fun FabMenuPreview() {
    TimeCalculatorTheme {
        Surface {
            FabMenu(
                isExpanded = false,
                onChangeExpanded = {},
                fabItems = listOf(
                    FabItem(
                        iconRes = R.drawable.home,
                        title = stringResource(R.string.fab_add_item),
                        contentDescription = stringResource(R.string.fab_add_item),
                        onClick = {}
                    ),
                    FabItem(
                        iconRes = R.drawable.home,
                        title = stringResource(R.string.fab_add_item),
                        contentDescription = stringResource(R.string.fab_add_item),
                        onClick = {}
                    ),
                )
            )
        }
    }
}


@Preview
@Composable
fun FabMenuPreviewExpanded() {
    com.xenikii.timecalculator.shared.theme.TimeCalculatorTheme {
        Surface {
            FabMenu(
                isExpanded = true,
                onChangeExpanded = {},
                horizontalAlignment = Alignment.CenterHorizontally,
                fabItems = listOf(
                    FabItem(
                        iconRes = R.drawable.home,
                        title = stringResource(R.string.fab_add_item),
                        contentDescription = stringResource(R.string.fab_add_item),
                        onClick = {}
                    ),
                    FabItem(
                        iconRes = R.drawable.home,
                        title = stringResource(R.string.fab_add_item),
                        contentDescription = stringResource(R.string.fab_add_item),
                        onClick = {}
                    ),
                )
            )
        }
    }
}