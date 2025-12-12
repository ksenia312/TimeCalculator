package com.example.morningcalculator.features.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.features.home.ui.components.BOTTOM_BAR_MAX_HEIGHT
import com.example.morningcalculator.features.home.ui.components.HomeBottomNavigationBar
import com.example.morningcalculator.features.home.ui.components.HomeTab
import com.example.morningcalculator.features.routineslist.ui.RoutinesListScreen
import com.example.morningcalculator.features.tasks.ui.TasksListScreen
import com.example.morningcalculator.shared.components.FabItem
import com.example.morningcalculator.shared.components.FabMenu
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    current: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onAddRoutine: () -> Unit = {},
    onAddTask: () -> Unit = {},
) {
    val isBarExpanded = rememberSaveable { mutableStateOf(false) }
    Surface {
        Box(Modifier.fillMaxSize()) {
            when (current) {
                HomeTab.ROUTINES -> RoutinesListScreen()
                HomeTab.TASKS -> TasksListScreen()
            }

            HomeBottomNavigationBar(
                selectedTab = current,
                onTabSelected = onTabSelected,
                centerButton = {
                    FabMenu(
                        isExpanded = isBarExpanded.value,
                        onChangeExpanded = { isBarExpanded.value = it },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        mainButtonAlignment = Alignment.BottomCenter,
                        fabItems = listOf(
                            FabItem(
                                icon = Icons.Default.Done,
                                title = "Task",
                                onClick = {
                                    isBarExpanded.value = false
                                    onAddTask()
                                },
                                contentDescription = ""
                            ),
                            FabItem(
                                icon = Icons.Default.Menu,
                                title = "Routine",
                                onClick = {
                                    isBarExpanded.value = false
                                    onAddRoutine()
                                },
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


fun Modifier.bottomIndent(): Modifier = this.padding(bottom = 24.dp + BOTTOM_BAR_MAX_HEIGHT.dp)


@PreviewAll
@Composable
fun HomeContentPreview() {
    PreviewTheme {
        HomeContent(
            current = HomeTab.ROUTINES,
            onTabSelected = {}
        )
    }
}