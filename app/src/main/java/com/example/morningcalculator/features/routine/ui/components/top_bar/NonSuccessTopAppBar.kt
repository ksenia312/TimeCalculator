package com.example.morningcalculator.features.routine.ui.components.top_bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.morningcalculator.shared.components.CustomTopBar
import com.example.morningcalculator.shared.components.CustomTopBarHeadingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonSuccessTopAppBar(
    title: String,
) {
    CustomTopBar(
        accentColor = Color.Red,
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
