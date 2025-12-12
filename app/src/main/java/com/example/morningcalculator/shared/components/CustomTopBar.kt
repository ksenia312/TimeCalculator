package com.example.morningcalculator.shared.components

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    val activity = LocalActivity.current as? ComponentActivity

    activity?.ChangeSystemTopBarTheme(onAccentColor)

    LargeTopAppBar(
        expandedHeight = 186.dp,
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
                modifier = Modifier.padding(bottom = 12.dp, end = 12.dp),
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
        })
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