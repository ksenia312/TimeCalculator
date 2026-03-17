package com.example.morningcalculator.features.home.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.ChangeSystemTopBarTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeAppBar(selectedTab: HomeTab) {
    ChangeSystemTopBarTheme(
        foreground = MaterialTheme.colorScheme.onSurface,
    )
    TopAppBar(
        title = {
            Text(
                text = when (selectedTab) {
                    HomeTab.ROUTINES -> "Routines"
                    HomeTab.TASKS -> "Tasks"
                },
                style = MaterialTheme.typography.titleLarge,
            )
        },
    )
//    val routinesCount = (homeViewState as? RoutinesListState.Success)?.routines?.size
//    val accentColor = LocalCustomColorScheme.current.accent
//
//    CustomTopBar(
//        accentColor = accentColor,
//        onAccentColor = MaterialTheme.colorScheme.onPrimary,
//        titleItems = {
//            CustomTopBarHeadingItem(
//                title = "${routinesCount ?: 0}",
//                subtitle = "Scheduled Routines",
//            )
//        },
//        actions = {}
//    )
}

@PreviewAll
@Composable
private fun HomeAppBarPreview() {
    PreviewTheme {
        HomeAppBar(selectedTab = HomeTab.ROUTINES)
    }
}