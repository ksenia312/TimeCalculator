package com.xenikii.timecalculator.features.home.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListState
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListViewModel
import com.xenikii.timecalculator.shared.components.AppScaffold
import com.xenikii.timecalculator.shared.components.DeleteConfirmationDialog
import com.xenikii.timecalculator.shared.components.FabItem
import com.xenikii.timecalculator.shared.components.FabMenu
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.LocalNavigator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = koinViewModel(),
    routinesViewModel: RoutinesListViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current
    val viewState = homeViewModel.uiState.collectAsStateWithLifecycle()
    val routinesViewState by routinesViewModel.viewState.collectAsStateWithLifecycle()
    val selectedIds by routinesViewModel.selectedIds.collectAsStateWithLifecycle()
    val showFab = viewState.value.selectedTab != HomeTab.SETTINGS
    val selectedTitles by remember {
        derivedStateOf {
            val state = routinesViewState
            if (state is RoutinesListState.Success) {
                state.items
                    .filter { it.routine.id in selectedIds }
                    .map { it.routine.title }
            } else emptyList()
        }
    }

    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    val isBarExpanded = rememberSaveable { mutableStateOf(false) }
    AppScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HomeAppBar(
                selectedTab = viewState.value.selectedTab,
                actions = {
                    if (viewState.value.selectedTab == HomeTab.ROUTINES && selectedIds.isNotEmpty()) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                            )
                        }
                    }
                }
            )
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

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            title = when (selectedTitles.size) {
                1 -> stringResource(R.string.routines_delete_single_title, selectedTitles.first())
                else -> stringResource(R.string.routines_delete_multiple_title, selectedTitles.size)
            },
            message = when (selectedTitles.size) {
                1 -> stringResource(R.string.routines_delete_single_message)
                else -> stringResource(
                    R.string.routines_delete_multiple_message,
                    selectedTitles.joinToString(", ")
                )
            },
            onConfirm = routinesViewModel::deleteSelected,
            onDismiss = { showDeleteConfirmation = false },
        )
    }
}
