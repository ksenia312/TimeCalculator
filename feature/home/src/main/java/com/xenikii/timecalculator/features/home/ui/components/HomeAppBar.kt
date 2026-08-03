package com.xenikii.timecalculator.features.home.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeAppBar(
    selectedTab: HomeTab,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = Modifier.shadow(6.dp),
        title = {
            Text(
                text = when (selectedTab) {
                    HomeTab.ROUTINES -> stringResource(R.string.home_tab_routines)
                    HomeTab.TASKS -> stringResource(R.string.home_tab_tasks)
                    HomeTab.SETTINGS -> stringResource(R.string.home_tab_settings)
                    HomeTab.LANDING -> ""
                },
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = actions,
    )
}

@PreviewAll
@Composable
private fun HomeAppBarPreview() {
    PreviewTheme {
        HomeAppBar(selectedTab = HomeTab.ROUTINES)
    }
}