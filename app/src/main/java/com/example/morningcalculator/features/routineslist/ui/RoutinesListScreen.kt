package com.example.morningcalculator.features.routineslist.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize(),
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
