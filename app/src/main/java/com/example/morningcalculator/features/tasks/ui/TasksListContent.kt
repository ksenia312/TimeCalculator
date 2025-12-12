package com.example.morningcalculator.features.tasks.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.morningcalculator.features.tasks.presentation.TasksListViewState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListContent(
    innerPadding: PaddingValues = PaddingValues(),
    viewState: TasksListViewState,
) {
    Box(Modifier.padding(innerPadding)) {


    }
}

