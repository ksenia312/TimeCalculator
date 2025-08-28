package com.example.morningcalculator.features.routine.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.features.routine.ui.views.EditRoutineFloatingButton
import com.example.morningcalculator.features.routine.ui.views.RoutineColorWrapper
import com.example.morningcalculator.features.routine.ui.views.TasksListView
import com.example.morningcalculator.features.routine.ui.views.top_bar.RoutineTopBar
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import com.example.morningcalculator.features.routine.view_model.RoutineViewState
import com.example.morningcalculator.shared.extensions.whenToGetUp
import org.koin.compose.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    id: String, viewModel: RoutineViewModel = RoutineViewModel(
        id = id, tasksRepository = getKoin().get(), routineRepository = getKoin().get()
    )
) {
    val viewState by viewModel.viewState.collectAsState()
    RoutineColorWrapper(viewState) {
        Scaffold(
            topBar = { RoutineTopBar(viewModel) },
            floatingActionButton = {
                val routine = (viewState as? RoutineViewState.Success)?.full
                if (routine != null) EditRoutineFloatingButton(routine, viewModel)
            }) {
            Box(modifier = Modifier.padding(it)) {
                when (val viewState = viewState) {
                    is RoutineViewState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is RoutineViewState.Success -> {
                        val combined = viewState.full
                        TasksListView(combined, viewModel)
                    }

                    is RoutineViewState.Error -> {
                        Text(text = viewState.error)
                    }
                }
            }
        }
    }
}