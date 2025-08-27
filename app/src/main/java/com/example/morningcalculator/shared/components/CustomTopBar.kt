package com.example.morningcalculator.features.home.ui.views

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import com.example.morningcalculator.shared.theme.ChangeSystemTopBarTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CustomTopBar(
    modifier: Modifier = Modifier,
    accentColor: Color,
    onAccentColor: Color,
    shape: Shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
    title: String? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    showNavigationIcon: Boolean = false,
    bottomRightWidget: @Composable () -> Unit = {}
) {
    val navigator = LocalNavHostController.current
    val activity = LocalActivity.current as? ComponentActivity

    activity?.ChangeSystemTopBarTheme(onAccentColor)

    LargeTopAppBar(
        expandedHeight = 186.dp,
        modifier = Modifier
            .background(
                color = accentColor,
                shape = shape
            )
            .then(modifier), actions = actions, title = {
            Row(
                Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier
                        .weight(1f)
                ) {
                    if (title != null) Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3
                    )
                    if (subtitle != null) Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3
                    )
                }
                bottomRightWidget()
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = onAccentColor,
            navigationIconContentColor = onAccentColor,
            actionIconContentColor = onAccentColor,
        ), navigationIcon = {
            if (showNavigationIcon) IconButton(onClick = { navigator.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, "back"
                )
            }
        })
}