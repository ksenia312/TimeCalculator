package com.example.morningcalculator.features.home.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.morningcalculator.features.home.presentation.HomeViewModel
import com.example.morningcalculator.features.home.ui.components.RoutineDialog
import com.example.morningcalculator.features.routine.ui.components.taskscreen.CreateTaskScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = koinViewModel()) {

    val viewState = homeViewModel.uiState.collectAsStateWithLifecycle()
    val showAddTaskDialog = remember { mutableStateOf(false) }
    val showAddRoutineDialog = remember { mutableStateOf(false) }

    if (showAddRoutineDialog.value) {
        RoutineDialog(onConfirm = { request ->
            homeViewModel.addRoutine(request)
            showAddRoutineDialog.value = false
        }, onDismiss = { showAddRoutineDialog.value = false })
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

    HomeContent(
        current = viewState.value.selectedTab,
        onTabSelected = homeViewModel::onTabSelected,
        onAddRoutine = {
            showAddRoutineDialog.value = true
        },
        onAddTask = {
            showAddTaskDialog.value = true
        }
    )
}
