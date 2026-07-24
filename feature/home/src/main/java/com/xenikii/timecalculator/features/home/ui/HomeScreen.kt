package com.xenikii.timecalculator.features.home.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.home.presentation.HomeViewModel
import com.xenikii.timecalculator.features.home.ui.components.HomeAppBar
import com.xenikii.timecalculator.features.home.ui.components.HomeBottomNavigationBar
import com.xenikii.timecalculator.features.home.ui.components.HomeTab
import com.xenikii.timecalculator.shared.components.AppScaffold
import com.xenikii.timecalculator.shared.components.FabItem
import com.xenikii.timecalculator.shared.components.FabMenu
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val viewState = homeViewModel.uiState.collectAsStateWithLifecycle()
    val showFab = viewState.value.selectedTab != HomeTab.SETTINGS

    val isBarExpanded = rememberSaveable { mutableStateOf(false) }
    AppScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HomeAppBar(viewState.value.selectedTab)
        },
        floatingActionButtonModifier = Modifier.padding(
            bottom = 64.dp
        ),
        bottomBar = {
            HomeBottomNavigationBar(
                selectedTab = viewState.value.selectedTab,
                onTabSelected = homeViewModel::onTabSelected,
            )
        },
        floatingActionButton = {
            if (showFab) {
                FabMenu(
                    isExpanded = isBarExpanded.value,
                    onChangeExpanded = { isBarExpanded.value = it },
                    horizontalAlignment = Alignment.End,
                    fabItems = listOf(
                        FabItem(
                            iconRes = R.drawable.task,
                            title = stringResource(R.string.fab_task),
                            onClick = {
                                isBarExpanded.value = false
                                navigator.navigateTo(AppRoute.CreateTask())
                            },
                            contentDescription = stringResource(R.string.fab_task)
                        ),
                        FabItem(
                            iconRes = R.drawable.routine,
                            title = stringResource(R.string.fab_routine),
                            onClick = {
                                isBarExpanded.value = false
                                navigator.navigateTo(AppRoute.CreateRoutine)
                            },
                            contentDescription = stringResource(R.string.fab_routine)
                        )
                    )
                )
            }
        }
    ) {
        HomeContent(
            modifier = Modifier,
            paddingValues = it,
            current = viewState.value.selectedTab,
            onCreateRoutineClick = { navigator.navigateTo(AppRoute.CreateRoutine) },
            onCreateTaskClick = { navigator.navigateTo(AppRoute.CreateTask()) },
            onLogoutClick = homeViewModel::logout,
            isLoggingOut = viewState.value.isLoggingOut,
        )
    }
}
