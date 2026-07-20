package com.example.morningcalculator.features.routine.ui.components.topbar

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.morningcalculator.shared.components.CustomTopBar
import com.example.morningcalculator.shared.features.routineCardBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonSuccessTopAppBar(
    title: String,
) {
    CustomTopBar(
        onAccentColor = MaterialTheme.colorScheme.onBackground,
        showNavigationIcon = true,
        modifier = Modifier.background(
            routineCardBackground(
                isOngoing = false,
                isCompleted = false
            )
        )
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
