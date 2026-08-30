package com.xenikii.timecalculator.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme

@Composable
fun HomeEmptyState(
    title: String,
    subtitle: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        AppButtonExpressive(
            onClick = onActionClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalCustomColorScheme.current.accent,
            )
        ) {
            Text(text = actionText)
        }
    }
}

@Preview
@Composable
fun HomeEmptyStatePreview() {
    PreviewTheme {
        HomeEmptyState(
            title = "No items found",
            subtitle = "You haven't added any items yet. Start by adding a new item to get started.",
            actionText = "Create first routine",
            onActionClick = {}
        )
    }
}