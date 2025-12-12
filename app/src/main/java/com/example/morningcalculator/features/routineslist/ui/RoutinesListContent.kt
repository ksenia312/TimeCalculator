package com.example.morningcalculator.features.routineslist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.home.ui.bottomIndent
import com.example.morningcalculator.features.routineslist.presentation.RoutinesListState
import com.example.morningcalculator.features.routineslist.ui.components.RoutineListItem
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewConstants
import com.example.morningcalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesListContent(
    innerPadding: PaddingValues = PaddingValues(0.dp),
    viewState: RoutinesListState,
    onEditRoutine: (Routine.Links) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        when (viewState) {
            is RoutinesListState.Loading -> {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .bottomIndent()
                )
            }

            is RoutinesListState.Success -> {
                val routines = viewState.sorted
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    item { Spacer(Modifier.height(16.dp)) }
                    routines.forEach { routine ->
                        item(key = routine.id) {
                            RoutineListItem(routine) {
                                onEditRoutine(it)
                            }
                        }
                    }
                    item { Box(Modifier.bottomIndent()) }
                }
            }

            is RoutinesListState.Error -> {
                val viewState = viewState
                Text(
                    text = viewState.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .bottomIndent()
                )
            }
        }
    }
}

@PreviewAll
@Composable
fun RoutineListContentPreview() {
    PreviewTheme {
        RoutinesListContent(
            viewState = RoutinesListState.Success(
                routines = PreviewConstants.routines,
                sorted = PreviewConstants.routines,
                sort = RoutinesListState.Sort.DEFAULT
            ),
            onEditRoutine = {}
        )
    }
}