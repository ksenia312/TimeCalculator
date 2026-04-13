package com.example.morningcalculator.features.home.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.ChangeSystemTopBarTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeAppBar(selectedTab: HomeTab) {
    ChangeSystemTopBarTheme(
        foreground = MaterialTheme.colorScheme.onSurface,
    )
    TopAppBar(
        modifier = Modifier.shadow(6.dp),
        title = {
            Text(
                text = when (selectedTab) {
                    HomeTab.ROUTINES -> "Routines"
                    HomeTab.TASKS -> "Tasks"
                    HomeTab.LANDING -> ""
                },
                style = MaterialTheme.typography.titleLarge,
            )
        },
    )
}

@PreviewAll
@Composable
private fun HomeAppBarPreview() {
    PreviewTheme {
        HomeAppBar(selectedTab = HomeTab.ROUTINES)
    }
}