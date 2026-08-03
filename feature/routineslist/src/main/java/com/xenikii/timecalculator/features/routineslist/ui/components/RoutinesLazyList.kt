package com.xenikii.timecalculator.features.routineslist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.features.routineslist.presentation.RoutineListItemState
import com.xenikii.timecalculator.shared.extensions.bottomIndent

@Composable
fun RoutinesLazyList(
    items: List<RoutineListItemState>,
    selectedIds: Set<String>,
    onLongPress: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        item { Spacer(Modifier.height(16.dp)) }
        items.forEach { routineItem ->
            item(key = routineItem.routine.id) {
                val isSelectionMode = selectedIds.isNotEmpty()
                val isSelected = routineItem.routine.id in selectedIds
                RoutineListItem(
                    item = routineItem,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onLongPress = { onLongPress(routineItem.routine.id) },
                    onToggleSelect = { onToggleSelect(routineItem.routine.id) },
                )
            }
        }
        item { Box(Modifier.bottomIndent()) }
    }
}
