package com.example.morningcalculator.features.tasks.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.morningcalculator.features.tasks.presentation.TasksListViewModel
import com.example.morningcalculator.features.tasks.ui.components.TasksListAppBar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(homeViewModel: TasksListViewModel = koinViewModel()) {
    val viewState = homeViewModel.viewState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TasksListAppBar(viewState.value)
        }
    ) { innerPadding ->
        TasksListContent(
            viewState = viewState.value,
            innerPadding = innerPadding
        )
    }
}
