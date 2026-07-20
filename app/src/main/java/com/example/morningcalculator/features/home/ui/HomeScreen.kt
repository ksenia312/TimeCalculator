package com.example.morningcalculator.features.home.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.morningcalculator.R
import com.example.morningcalculator.features.home.presentation.HomeViewModel
import com.example.morningcalculator.features.home.ui.components.HomeAppBar
import com.example.morningcalculator.features.home.ui.components.RoutineDialog
import com.example.morningcalculator.features.routine.ui.components.taskscreen.CreateTaskScreen
import com.example.morningcalculator.shared.components.AppScaffold
import com.example.morningcalculator.shared.components.FabItem
import com.example.morningcalculator.shared.components.FabMenu
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = koinViewModel()) {

    val viewState = homeViewModel.uiState.collectAsStateWithLifecycle()
    val showAddTaskDialog = remember { mutableStateOf(false) }
    val routineDialogViewState = viewState.value.routineDialogViewState

    if (routineDialogViewState.isVisible) {
        RoutineDialog(
            screenTitle = stringResource(R.string.routine_dialog_add_title),
            onConfirm = homeViewModel::onRoutineDialogConfirm,
            onDismiss = homeViewModel::onRoutineDialogDismiss,
            viewState = routineDialogViewState,
            onStateChange = homeViewModel::onRoutineDialogViewStateChange,
        )
    }

    if (showAddTaskDialog.value) {
        CreateTaskScreen(
            linkedToRoutine = false,
            onConfirm = { request, selectedIndex ->
                homeViewModel.addNewTask(request)
                showAddTaskDialog.value = false
            }, onDismiss = { showAddTaskDialog.value = false }
        )
    }

    val isBarExpanded = rememberSaveable { mutableStateOf(false) }
    AppScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HomeAppBar(viewState.value.selectedTab)
        },
        floatingActionButtonModifier = Modifier.padding(
            bottom = 64.dp
        ),
        floatingActionButton = {
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
                            showAddTaskDialog.value = true
                        },
                        contentDescription = stringResource(R.string.fab_task)
                    ),
                    FabItem(
                        iconRes = R.drawable.routine,
                        title = stringResource(R.string.fab_routine),
                        onClick = {
                            isBarExpanded.value = false
                            homeViewModel.onAddRoutineClick()
                        },
                        contentDescription = stringResource(R.string.fab_routine)
                    )
                )
            )
        }
    ) {
        HomeContent(
            paddingValues = it,
            current = viewState.value.selectedTab,
            onTabSelected = homeViewModel::onTabSelected,
            onCreateRoutineClick = homeViewModel::onAddRoutineClick,
            onCreateTaskClick = { showAddTaskDialog.value = true },
        )
    }
}
