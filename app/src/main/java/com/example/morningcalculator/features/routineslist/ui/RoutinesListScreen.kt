package com.example.morningcalculator.features.routineslist.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.morningcalculator.features.routineslist.presentation.RoutinesListViewModel
import com.example.morningcalculator.features.routineslist.ui.components.RoutinesListAppBar
import com.example.morningcalculator.shared.components.AppScaffold
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesListScreen(routinesViewModel: RoutinesListViewModel = koinViewModel()) {
    val viewState = routinesViewModel.viewState.collectAsStateWithLifecycle()

    AppScaffold(
        topBar = {
            RoutinesListAppBar(viewState.value)
        }
    ) { innerPadding ->
        RoutinesListContent(
            innerPadding = innerPadding,
            viewState = viewState.value,
            onEditRoutine = routinesViewModel::editRoutine
        )
    }
}
