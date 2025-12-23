package com.example.morningcalculator.features.routineslist.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.morningcalculator.features.routineslist.presentation.RoutinesListState
import com.example.morningcalculator.shared.components.CustomTopBar
import com.example.morningcalculator.shared.components.CustomTopBarHeadingItem
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewConstants
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RoutinesListAppBar(homeViewState: RoutinesListState) {
    val routinesCount = (homeViewState as? RoutinesListState.Success)?.routines?.size
    val accentColor = LocalCustomColorScheme.current.accent

    CustomTopBar(
        accentColor = accentColor,
        onAccentColor = MaterialTheme.colorScheme.onPrimary,
        titleItems = {
            CustomTopBarHeadingItem(
                title = "${routinesCount ?: 0}",
                subtitle = "Scheduled Routines",
            )
        },
        actions = {}
    )
}

@PreviewAll
@Composable
private fun HomeAppBarPreview() {
    PreviewTheme {
        RoutinesListAppBar(
            homeViewState = RoutinesListState.Success(
                routines = PreviewConstants.routinesFull,
                sorted = PreviewConstants.routinesFull,
                sort = RoutinesListState.Sort.DEFAULT
            )
        )
    }
}