package com.example.morningcalculator.features.routine.ui.views.top_bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.morningcalculator.features.home.ui.views.CustomTopBar
import com.example.morningcalculator.features.home.ui.views.CustomTopBarHeading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonSuccessTopAppBar(
    title: String,
) {
    CustomTopBar(
        accentColor = Color.Red,
        onAccentColor = Color.White,
        showNavigationIcon = true,
        headings = listOf(
            CustomTopBarHeading(
                title = title,
                subtitle = "",
            )
        ),
    )
}
