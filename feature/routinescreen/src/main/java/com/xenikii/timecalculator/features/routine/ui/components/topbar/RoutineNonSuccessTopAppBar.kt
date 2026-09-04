package com.xenikii.timecalculator.features.routine.ui.components.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.shared.components.CustomTopBar
import com.xenikii.timecalculator.shared.features.routineCardBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonSuccessTopAppBar(
    title: String? = null,
) {
    CustomTopBar(
        onAccentColor = MaterialTheme.colorScheme.onPrimary,
        showNavigationIcon = true,
        modifier = Modifier
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                routineCardBackground(
                    isOngoing = false,
                    isCompleted = false
                )
            )
    ) {
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
