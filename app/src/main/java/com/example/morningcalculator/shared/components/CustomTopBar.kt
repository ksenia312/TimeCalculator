package com.example.morningcalculator.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.shared.extensions.endAt
import com.example.morningcalculator.shared.extensions.stringDateTime
import com.example.morningcalculator.shared.extensions.whenToStart
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewConstants
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.ChangeSystemTopBarTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CustomTopBar(
    modifier: Modifier = Modifier,
    accentColor: Color,
    onAccentColor: Color,
    shape: Shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
    titleItems: @Composable RowScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    showNavigationIcon: Boolean = false,
) {
    ChangeSystemTopBarTheme(onAccentColor)

    LargeTopAppBar(
        expandedHeight = 140.dp,
        modifier = Modifier
            .clip(shape)
            .background(
                color = accentColor,
                shape = shape
            )
            .then(modifier),
        actions = actions,
        title = {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                titleItems()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = onAccentColor,
            navigationIconContentColor = onAccentColor,
            actionIconContentColor = onAccentColor,
        ),
        navigationIcon = {
            if (showNavigationIcon) BackButton(
                color = onAccentColor
            )
        }
    )
}

@Composable
fun CustomTopBarHeadingItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )
    }
}

@PreviewAll
@Composable
fun CustomTopBarPreview() {
    val routine = PreviewConstants.routinesFull.first()
    PreviewTheme {
        CustomTopBar(
            accentColor = Color.Blue,
            onAccentColor = Color.White,
            titleItems = {
                CustomTopBarHeadingItem(
                    title = routine.whenToStart().stringDateTime(),
                    subtitle = "Start at",
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                CustomTopBarHeadingItem(
                    title = routine.endAt().stringDateTime(),
                    subtitle = "End at",
                )
            },
            actions = {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        "Modified at",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.End
                    )
                    Text(
                        routine.modifiedAt.stringDateTime(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
            },
            showNavigationIcon = true,
        )
    }
}