package com.xenikii.timecalculator.features.routine.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.xenikii.timecalculator.shared.components.AppCircleIndicator

/**
 * Leading content of a routine task row: a completion indicator normally, swapped for a
 * selection checkbox while the list is in edit mode.
 */
@Composable
fun RoutineTaskSelectionIndicator(
    isEditMode: Boolean,
    isSelected: Boolean,
    isCompleted: Boolean,
) {
    if (isEditMode) {
        Icon(
            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        val accentColor = LocalRoutineColor.current
        val onAccentColor = LocalRoutineColor.current.copy(alpha = 0.05f)
        AppCircleIndicator(
            backgroundColor = if (isCompleted) accentColor else onAccentColor,
            foregroundColor = if (isCompleted) MaterialTheme.colorScheme.onPrimary else accentColor,
        )
    }
}
