package com.xenikii.timecalculator.features.routineeditor.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.RoutineScheduleAnchor
import com.xenikii.timecalculator.shared.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAnchorSelector(
    options: List<Pair<RoutineScheduleAnchor, String>>,
    anchor: RoutineScheduleAnchor,
    onChanged: (RoutineScheduleAnchor) -> Unit,
) {
    var anchorExpanded by remember { mutableStateOf(false) }
    val anchorLabel = options.firstOrNull { it.first == anchor }?.second
        ?: stringResource(R.string.routine_anchor_start_at)

    ExposedDropdownMenuBox(
        expanded = anchorExpanded,
        onExpandedChange = { anchorExpanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        AppTextField(
            value = anchorLabel,
            onValueChange = {},
            readOnly = true,
            interactionSource = null,
            label = { Text(stringResource(R.string.label_anchor)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = anchorExpanded
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = anchorExpanded,
            onDismissRequest = { anchorExpanded = false }
        ) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onChanged(value)
                        anchorExpanded = false
                    }
                )
            }
        }
    }
}