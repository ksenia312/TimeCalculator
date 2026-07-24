package com.xenikii.timecalculator.features.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.theme.TimeCalculatorTheme

@Composable
fun SettingsContent(
    onLogoutClick: () -> Unit,
    isLoggingOut: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onLogoutClick,
            enabled = !isLoggingOut,
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(16.dp)
                )
            } else {
                Text(text = stringResource(R.string.settings_action_logout))
            }
        }
    }
}

@PreviewAll
@Composable
private fun SettingsContentPreview() {
    TimeCalculatorTheme {
        SettingsContent(
            onLogoutClick = {},
            isLoggingOut = false,
        )
    }
}