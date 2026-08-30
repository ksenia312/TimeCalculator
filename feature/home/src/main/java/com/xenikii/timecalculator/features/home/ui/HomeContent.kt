package com.xenikii.timecalculator.features.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.features.home.ui.components.HomeTab
import com.xenikii.timecalculator.features.landing.ui.LandingScreen
import com.xenikii.timecalculator.features.routineslist.ui.RoutinesListScreen
import com.xenikii.timecalculator.features.settings.ui.SettingsScreen
import com.xenikii.timecalculator.features.tasks.ui.TasksListScreen
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    current: HomeTab,
    isSyncing: Boolean,
    onCreateRoutineClick: () -> Unit,
    onCreateTaskClick: () -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (current) {
            HomeTab.LANDING -> LandingScreen(
                onCreateRoutineClick = onCreateRoutineClick,
                isSyncing = isSyncing,
            )
            HomeTab.ROUTINES -> RoutinesListScreen(
                onCreateRoutineClick = onCreateRoutineClick,
                isSyncing = isSyncing,
            )
            HomeTab.TASKS -> TasksListScreen(
                onCreateTaskClick = onCreateTaskClick,
                isSyncing = isSyncing,
            )
            HomeTab.SETTINGS -> SettingsScreen()
        }
    }
}

@PreviewAll
@Composable
fun HomeContentPreview() {
    PreviewTheme {
        HomeContent(
            current = HomeTab.ROUTINES,
            isSyncing = false,
            onCreateRoutineClick = {},
            onCreateTaskClick = {},
            paddingValues = PaddingValues(0.dp),
        )
    }
}