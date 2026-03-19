package com.example.morningcalculator.shared.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingActionButtonMenuScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R

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
    val rotation: Float by animateFloatAsState(if (isExpanded) 225f else 0f)
    val colors = ButtonDefaults.elevatedButtonColors(
        containerColor = itemContainerColor, contentColor = itemContentColor
    )

    BackHandler(isExpanded) { onChangeExpanded(false) }
    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = isExpanded,
        horizontalAlignment = horizontalAlignment,
        button = {
            FloatingActionButton(
                onClick = {
                    onChangeExpanded(!isExpanded)
                },
                containerColor = mainContainerColor,
                contentColor = mainContentColor,
                shape = RoundedCornerShape(size = 100.dp),
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Add else mainImageVector,
                    contentDescription = if (isExpanded) "Close menu" else "Open menu",
                    modifier = Modifier.rotate(rotation)
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
    Surface {
        FabMenu(
            isExpanded = false,
            onChangeExpanded = {},
            fabItems = listOf(
                FabItem(
                    iconRes = R.drawable.home,
                    title = "Add Item",
                    contentDescription = "Add Item",
                    onClick = {}
                ),
                FabItem(
                    iconRes = R.drawable.home,
                    title = "Add Item",
                    contentDescription = "Add Item",
                    onClick = {}
                ),
            )
        )
    }
}


@Preview
@Composable
fun FabMenuPreviewExpanded() {
    Surface {
        FabMenu(
            isExpanded = true,
            onChangeExpanded = {},
            horizontalAlignment = Alignment.CenterHorizontally,
            fabItems = listOf(
                FabItem(
                    iconRes = R.drawable.home,
                    title = "Add Item",
                    contentDescription = "Add Item",
                    onClick = {}
                ),
                FabItem(
                    iconRes = R.drawable.home,
                    title = "Add Item",
                    contentDescription = "Add Item",
                    onClick = {}
                ),
            )
        )
    }
}