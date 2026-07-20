package com.example.morningcalculator.features.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.features.home.ui.components.BOTTOM_BAR_MAX_HEIGHT
import com.example.morningcalculator.features.home.ui.components.HomeBottomNavigationBar
import com.example.morningcalculator.features.home.ui.components.HomeTab
import com.example.morningcalculator.features.landing.ui.LandingScreen
import com.example.morningcalculator.features.routineslist.ui.RoutinesListScreen
import com.example.morningcalculator.features.tasks.ui.TasksListScreen
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    current: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onCreateRoutineClick: () -> Unit,
    onCreateTaskClick: () -> Unit,
    paddingValues: PaddingValues
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(
                PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                )
            )
    ) {
        when (current) {
            HomeTab.LANDING -> LandingScreen(onCreateRoutineClick = onCreateRoutineClick)
            HomeTab.ROUTINES -> RoutinesListScreen(onCreateRoutineClick = onCreateRoutineClick)
            HomeTab.TASKS -> TasksListScreen(onCreateTaskClick = onCreateTaskClick)
        }

        HomeBottomNavigationBar(
            selectedTab = current,
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

fun Modifier.bottomIndent(): Modifier = this.padding(bottom = 24.dp + BOTTOM_BAR_MAX_HEIGHT.dp)

@PreviewAll
@Composable
fun HomeContentPreview() {
    PreviewTheme {
        HomeContent(
            current = HomeTab.ROUTINES,
            onTabSelected = { },
            onCreateRoutineClick = {},
            onCreateTaskClick = {},
            paddingValues = PaddingValues(0.dp)
        )
    }
}