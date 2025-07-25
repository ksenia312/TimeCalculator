package com.example.morningcalculator.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.morningcalculator.features.home.ui.views.RoutineDialog
import com.example.morningcalculator.features.home.ui.views.RoutineListItem
import com.example.morningcalculator.features.home.view_model.HomeViewModel
import com.example.morningcalculator.features.home.view_model.HomeViewState
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = koinViewModel()) {
    val viewState by homeViewModel.viewState.collectAsState()
    val showAddRoutineDialog = remember { mutableStateOf(false) }
    val navigator = LocalNavHostController.current

    if (showAddRoutineDialog.value) {
        RoutineDialog(onConfirm = { request ->
            homeViewModel.addRoutine(request)
            showAddRoutineDialog.value = false
        }, onDismiss = { showAddRoutineDialog.value = false })
    }

    Surface {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(title = {
                Text(text = "Morning Calculator")
            })
        }, floatingActionButton = {
            FloatingActionButton(onClick = {
                showAddRoutineDialog.value = true
            }) {
                Icon(Icons.Default.Add, "Add Routine")
            }
        }) { innerPadding ->
            when (viewState) {
                is HomeViewState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is HomeViewState.Success -> {
                    val tasks = (viewState as HomeViewState.Success).tasks
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        tasks.forEach { routine ->
                            item(key = routine.id) {
                                RoutineListItem(routine, navigator) {
                                    homeViewModel.editRoutine(it)
                                }
                            }
                        }
                    }
                }

                is HomeViewState.Error -> {
                    val viewState = viewState as HomeViewState.Error
                    Text(text = viewState.error)
                }
            }
        }
    }
}