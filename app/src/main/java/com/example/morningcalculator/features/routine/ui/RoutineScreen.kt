package com.example.morningcalculator.features.routine.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.home.ui.views.CustomTopBar
import com.example.morningcalculator.features.home.ui.views.RoutineDialog
import com.example.morningcalculator.features.routine.ui.views.TasksBottomSheet
import com.example.morningcalculator.features.routine.ui.views.TasksListView
import com.example.morningcalculator.features.routine.ui.views.task_dialog.AddTaskScreen
import com.example.morningcalculator.features.routine.view_model.RoutineViewModel
import com.example.morningcalculator.features.routine.view_model.RoutineViewState
import com.example.morningcalculator.shared.extensions.formatAsDateTime
import com.example.morningcalculator.shared.extensions.whenToGetUp
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import org.koin.compose.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    id: String, viewModel: RoutineViewModel = RoutineViewModel(
        id = id, tasksRepository = getKoin().get(), routineRepository = getKoin().get()
    )
) {
    val showTasksSheet = remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    val editingRoutine = remember { mutableStateOf<Routine.Links?>(null) }
    val viewState by viewModel.viewState.collectAsState()

    if (showTasksSheet.value) {
        TasksBottomSheet(
            onDismiss = { showTasksSheet.value = false }, viewModel = viewModel
        )
    }
    if (showAddTaskDialog) {
        AddTaskScreen(onConfirm = { request, selectedIndex ->
            viewModel.addNewTask(request, selectedIndex)
            showAddTaskDialog = false
        }, onDismiss = { showAddTaskDialog = false })
    }
    if (editingRoutine.value != null) {
        val routine = editingRoutine.value!!
        RoutineDialog(initialRoutine = routine, onConfirm = { request ->
            viewModel.editRoutine(
                routine.copy(
                    title = request.title, time = request.time
                )
            )
            editingRoutine.value = null
        }, onDismiss = {
            editingRoutine.value = null
        })
    }
    Scaffold(topBar = {
        when (val viewState = viewState) {
            is RoutineViewState.Success -> {
                SuccessTopAppBar(viewState, editingRoutine, showTasksSheet)
            }

            is RoutineViewState.Error -> NonSuccessTopAppBar("Error")
            is RoutineViewState.Loading -> NonSuccessTopAppBar("Loading")
        }
    }, floatingActionButton = {
        FloatingActionButton(onClick = {
            showAddTaskDialog = true
        }) {
            Icon(Icons.Default.Add, "Add Task")
        }
    }) {
        Box(modifier = Modifier.padding(it)) {
            when (val viewState = viewState) {
                is RoutineViewState.Loading -> {
                    CircularProgressIndicator()
                }

                is RoutineViewState.Success -> {
                    SuccessView(viewState, viewModel)
                }

                is RoutineViewState.Error -> {
                    Text(text = viewState.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessTopAppBar(
    viewState: RoutineViewState.Success,
    showEditRoutineDialog: MutableState<Routine.Links?>,
    showTasksSheet: MutableState<Boolean>
) {
    val routine = viewState.full

    CustomTopBar(
        subtitle = routine.title,
        accentColor = Color.Red,
        onAccentColor = Color.White,
        actions = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Modified at",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.End
                )
                Text(
                    routine.modifiedAt.formatAsDateTime(
                        overridePattern = "dd.MM.yyyy HH:mm"
                    ), style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
//            IconButton(onClick = { showTasksSheet.value = true }) {
//                Icon(Icons.Default.Search, "search")
//            }
        },
        modifier = Modifier.clickable(
            onClick = {
                showEditRoutineDialog.value = viewState.links
            },
        ),
        showNavigationIcon = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NonSuccessTopAppBar(
    title: String,
) {
    CustomTopBar(
        subtitle = title,
        accentColor = Color.Red,
        onAccentColor = Color.White,
        showNavigationIcon = true
    )
}

@Composable
private fun SuccessView(viewState: RoutineViewState.Success, viewModel: RoutineViewModel) {
    Column {
        val combined = viewState.full
        TasksListView(combined, viewModel)
        Box(modifier = Modifier.padding(8.dp)) {
            Column {
                Text(
                    "Wake up at ${combined.whenToGetUp()}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "Leave at ${combined.time}", style = MaterialTheme.typography.titleLarge
                )
            }
        }

    }
}
