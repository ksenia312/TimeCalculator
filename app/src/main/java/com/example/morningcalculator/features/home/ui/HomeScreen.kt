package com.example.morningcalculator.features.home.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.morningcalculator.core.repository.PreviewRoutineRepository
import com.example.morningcalculator.core.repository.PreviewTasksRepository
import com.example.morningcalculator.features.home.ui.views.BOTTOM_BAR_MAX_HEIGHT
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
import com.example.morningcalculator.shared.theme.PreviewAll
import com.example.morningcalculator.shared.theme.PreviewTheme
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(homeViewModel: HomeViewModel = koinViewModel()) {
    val isBarExpanded = rememberSaveable { mutableStateOf(false) }
    var current by rememberSaveable { mutableStateOf(HomeTab.ROUTINES) }
    val showAddRoutineDialog = remember { mutableStateOf(false) }

    Surface {
        if (showAddRoutineDialog.value) {
            RoutineDialog(onConfirm = { request ->
                homeViewModel.addRoutine(request)
                showAddRoutineDialog.value = false
            }, onDismiss = { showAddRoutineDialog.value = false })
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                when (current) {
                    HomeTab.ROUTINES -> HomeAppBar(homeViewModel)
                    HomeTab.TASKS -> {}
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (current) {
                    HomeTab.ROUTINES -> RoutineListContent(homeViewModel)

                    HomeTab.TASKS -> Text(
                        text = "Tasks",
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                HomeBottomNavigationBar(
                    selectedTab = current,
                    onTabSelected = { current = it },
                    centerButton = {
                        FabMenu(
                            isExpanded = isBarExpanded.value,
                            onChangeExpanded = { isBarExpanded.value = it },
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
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineListContent(
    homeViewModel: HomeViewModel
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
                item { Spacer(Modifier.height(24.dp + BOTTOM_BAR_MAX_HEIGHT.dp)) }
            }
        }

        is HomeViewState.Error -> {
            val viewState = viewState as HomeViewState.Error
            Text(text = viewState.error)
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@PreviewAll
@Composable
fun RoutineListScreenPreview() {
    PreviewTheme {
        RoutineListScreen(
            homeViewModel = HomeViewModel(
                repository = PreviewTasksRepository(),
                routineRepository = PreviewRoutineRepository()
            )
        )
    }
}