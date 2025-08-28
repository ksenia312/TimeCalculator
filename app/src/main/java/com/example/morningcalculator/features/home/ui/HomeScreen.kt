package com.example.morningcalculator.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.features.home.ui.views.HomeAppBar
import com.example.morningcalculator.features.home.ui.views.HomeBottomNavigationBar
import com.example.morningcalculator.features.home.ui.views.HomeTab
import com.example.morningcalculator.features.home.ui.views.RoutineDialog
import com.example.morningcalculator.features.home.ui.views.RoutineListItem
import com.example.morningcalculator.features.home.view_model.HomeViewModel
import com.example.morningcalculator.features.home.view_model.HomeViewState
import com.example.morningcalculator.shared.components.FabItem
import com.example.morningcalculator.shared.components.FabMenu
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(homeViewModel: HomeViewModel = koinViewModel()) {
    var current by rememberSaveable { mutableStateOf(HomeTab.ROUTINES) }
    val showAddRoutineDialog = remember { mutableStateOf(false) }

    Surface {
        if (showAddRoutineDialog.value) {
            RoutineDialog(onConfirm = { request ->
                homeViewModel.addRoutine(request)
                showAddRoutineDialog.value = false
            }, onDismiss = { showAddRoutineDialog.value = false })
        }

        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            when (current) {
                HomeTab.ROUTINES -> HomeAppBar(homeViewModel)
                HomeTab.TASKS -> {}
            }
        }, bottomBar = {
            HomeBottomNavigationBar(
                selectedTab = current,
                onTabSelected = { current = it },
                centerButton = {
                    FabMenu(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        mainButtonAlignment = Alignment.BottomCenter,
                        fabItems = listOf(
                            FabItem(
                                icon = Icons.Default.Add,
                                title = "Add Routine",
                                onClick = { showAddRoutineDialog.value = true },
                                contentDescription = ""
                            )
                        )
                    )
                })
        }) { innerPadding ->
            when (current) {
                HomeTab.ROUTINES -> RoutineListScreen(
                    homeViewModel, innerPadding
                )

                HomeTab.TASKS -> Text(text = "Tasks")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineListScreen(
    homeViewModel: HomeViewModel,
    innerPadding: PaddingValues,
) {
    val viewState by homeViewModel.viewState.collectAsState()
    val navigator = LocalNavHostController.current

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