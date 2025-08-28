package com.example.morningcalculator.shared.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

data class FabItem(
    val icon: ImageVector,
    val title: String,
    val contentDescription: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FabMenu(
    fabItems: List<FabItem>,
    modifier: Modifier = Modifier,
    mainContainerColor: Color = MaterialTheme.colorScheme.primary,
    mainContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    itemContainerColor: Color = MaterialTheme.colorScheme.primary,
    itemContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    mainImageVector: ImageVector = Icons.Default.Add,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
    mainButtonAlignment: Alignment = Alignment.BottomEnd
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation: Float by animateFloatAsState(if (expanded) 225f else 0f)
    val colors = ButtonDefaults.elevatedButtonColors(
        containerColor = itemContainerColor, contentColor = itemContentColor
    )

    val specFloat: TweenSpec<Float> = tween(150)
    val spec: TweenSpec<IntSize> = tween(150)
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = mainButtonAlignment
    ) {
//        if (expanded) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Transparent)
//                    .clickable(
//                        interactionSource = remember { MutableInteractionSource() },
//                        indication = null
//                    ) { expanded = false }
//            )
//        }
        Column(horizontalAlignment = horizontalAlignment) {
            fabItems.forEachIndexed { index, item ->
                AnimatedVisibility(
                    visible = expanded, enter = fadeIn(animationSpec = specFloat) + expandIn(
                        expandFrom = mainButtonAlignment, animationSpec = spec
                    ), exit = fadeOut(animationSpec = specFloat) + shrinkOut(
                        shrinkTowards = mainButtonAlignment, animationSpec = spec
                    )
                ) {
                    ElevatedButtonWithIconM3(
                        colors = colors,
                        onClick = {
                            item.onClick()
                            expanded = false
                        },
                        text = item.title,
                        imageVector = item.icon,
                        contentDescription = item.contentDescription,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = mainContainerColor,
                contentColor = mainContentColor,
                shape = RoundedCornerShape(size = 100.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Add else mainImageVector,
                    contentDescription = if (expanded) "Close menu" else "Open menu",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
fun ElevatedButtonWithIconM3(
    onClick: () -> Unit,
    text: String,
    imageVector: ImageVector,
    contentDescription: String,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors()
) {
    ElevatedButton(
        onClick = onClick, modifier = Modifier.padding(0.dp), colors = colors
    ) {
        Icon(
            imageVector = imageVector, contentDescription = contentDescription
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}