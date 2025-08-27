package com.example.morningcalculator.features.routine.ui.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.morningcalculator.features.routine.view_model.RoutineViewState
import com.example.morningcalculator.shared.extensions.toColor

@Composable
fun RoutineColorWrapper(viewState: RoutineViewState, content: @Composable () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val routineColor by remember(viewState) {
        derivedStateOf {
            (viewState as? RoutineViewState.Success)?.full?.color?.toColor() ?: primary
        }
    }

    CompositionLocalProvider(LocalRoutineColor provides routineColor) {
        content()
    }
}

val LocalRoutineColor = compositionLocalOf {
    Color.Unspecified
}