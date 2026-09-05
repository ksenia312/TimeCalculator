package com.xenikii.timecalculator.features.routine.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.SubData
import com.xenikii.timecalculator.shared.components.AppElevatedButtonMedium
import com.xenikii.timecalculator.shared.extensions.shortStringValue
import kotlin.time.Duration

/**
 * Trailing content of a routine task row outside edit mode: picks the duration to schedule
 * the task's linked [RoutineLink] with, from its available durations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineTaskDurationDropdown(
    link: RoutineLink,
    onDurationSelected: (SubData) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val selectedDuration = link.subData?.duration ?: Duration.ZERO

    ExposedDropdownMenuBox(
        expanded = menuExpanded,
        onExpandedChange = { menuExpanded = !menuExpanded },
    ) {
        AppElevatedButtonMedium(
            onClick = { },
            contentPadding = ButtonDefaults.ContentPadding,
            modifier = Modifier.menuAnchor(
                type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                enabled = true,
            ),
        ) {
            Box {
                Text(
                    selectedDuration.takeIf { it > Duration.ZERO }?.shortStringValue()
                        ?: stringResource(R.string.task_set_duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedDuration > Duration.ZERO) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = menuExpanded,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(24.dp),
                )
            }
        }

        ExposedDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            link.task.dataSortedByDuration.forEach { sub ->
                DropdownMenuItem(
                    text = { Text(sub.duration.shortStringValue()) },
                    onClick = {
                        menuExpanded = false
                        onDurationSelected(sub)
                    },
                )
            }
        }
    }
}
