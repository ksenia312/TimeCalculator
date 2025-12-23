package com.example.morningcalculator.features.routine.ui.components.topbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.morningcalculator.shared.components.CustomTopBar
import com.example.morningcalculator.shared.components.CustomTopBarHeadingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingTopAppBar(
    title: String,
) {
    CustomTopBar(
        accentColor = MaterialTheme.colorScheme.background,
        onAccentColor = MaterialTheme.colorScheme.onBackground,
        showNavigationIcon = true,
        titleItems = {
            CustomTopBarHeadingItem(
                title = title,
                subtitle = "",
            )
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonSuccessTopAppBar(
    title: String,
) {
    CustomTopBar(
        accentColor = MaterialTheme.colorScheme.error,
        onAccentColor = Color.White,
        showNavigationIcon = true,
        titleItems = {
            CustomTopBarHeadingItem(
                title = title,
                subtitle = "",
            )
        }
    )
}
