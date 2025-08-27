package com.example.morningcalculator.features.routine.ui.views.top_bar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.home.ui.views.CustomTopBar
import com.example.morningcalculator.features.routine.ui.views.LocalRoutineColor
import com.example.morningcalculator.features.routine.view_model.RoutineViewState
import com.example.morningcalculator.shared.extensions.formatAsDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessTopAppBar(
    viewState: RoutineViewState.Success,
    showEditRoutineDialog: MutableState<Routine.Links?>,
) {
    val routine = viewState.full
    val routineColor = LocalRoutineColor.current

    CustomTopBar(
        subtitle = routine.title,
        accentColor = LocalRoutineColor.current,
        onAccentColor = if (routineColor.luminance() < 0.5f) Color.White else Color.Black,
        actions = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Modified at",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.End
                )
                Text(
                    routine.modifiedAt.formatAsDateTime(
                        overridePattern = "dd.MM.yyyy HH:mm"
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
//            IconButton(onClick = { showTasksSheet.value = true }) {
//                Icon(Icons.Default.Search, "search")
//            }
        },
        modifier = Modifier.clickable(
            onClick = {
                showEditRoutineDialog.value = viewState.links
            },
        ),
        showNavigationIcon = true,
    )
}