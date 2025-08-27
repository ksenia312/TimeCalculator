package com.example.morningcalculator.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.features.home.ui.views.HomeAppBar
import com.example.morningcalculator.features.home.ui.views.RoutineDialog
import com.example.morningcalculator.features.home.ui.views.RoutineListItem
import com.example.morningcalculator.features.home.view_model.HomeViewModel
import com.example.morningcalculator.features.home.view_model.HomeViewState
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
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
            HomeAppBar(viewState)
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
                    val routines = (viewState as HomeViewState.Success).sorted
                    LazyColumn(
                        modifier = Modifier.padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item { Spacer(Modifier.height(16.dp)) }
                        routines.forEach { routine ->
                            item(key = routine.id) {
                                RoutineListItem(routine, navigator) {
                                    homeViewModel.editRoutine(it)
                                }
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
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