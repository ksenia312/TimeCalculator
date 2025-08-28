package com.example.morningcalculator.features.home.ui.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.morningcalculator.features.home.view_model.HomeViewState
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeAppBar(viewState: HomeViewState) {
    val routinesCount = (viewState as? HomeViewState.Success)?.routines?.size
    val accentColor = LocalCustomColorScheme.current.accent

    CustomTopBar(
        accentColor = accentColor,
        onAccentColor = MaterialTheme.colorScheme.onPrimary,
        headings = listOf(
            CustomTopBarHeading(
                title = "${routinesCount ?: 0}",
                subtitle = "Scheduled Routines",
            )
        ),
        actions = {}
    )
}