package com.example.morningcalculator.features.routine.ui.components.top_bar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.routine.ui.components.LocalRoutineColor
import com.example.morningcalculator.features.routine.presentation.RoutineViewState
import com.example.morningcalculator.shared.components.CustomTopBar
import com.example.morningcalculator.shared.components.CustomTopBarHeadingItem
import com.example.morningcalculator.shared.extensions.formatAsDateTime
import com.example.morningcalculator.shared.extensions.whenToStart
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessTopAppBar(
    viewState: RoutineViewState.Success,
    onShowEditDialog: () -> Unit = { },
) {
    val routine = viewState.full
    val routineColor = LocalRoutineColor.current

    CustomTopBar(
        titleItems = {
            CustomTopBarHeadingItem(
                title = routine.whenToStart().toString(),
                subtitle = "Start at",
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            CustomTopBarHeadingItem(
                title = routine.time.toString(),
                subtitle = "End at",
            )
        },
        accentColor = LocalRoutineColor.current,
        onAccentColor = if (routineColor.luminance() < 0.5f) Color.White else Color.Black,
        actions = {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    "Modified at",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End
                )
                Text(
                    routine.modifiedAt.formatAsDateTime(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        },
        modifier = Modifier.clickable(
            onClick = {
                onShowEditDialog()
            },
        ),
        showNavigationIcon = true,
    )
}

@PreviewAll
@Composable
fun SuccessTopAppBarPreview() {
    val routine = Routine.Full(
        id = "1",
        title = "Morning Routine",
        time = kotlinx.datetime.LocalTime(8, 0),
        modifiedAt = System.currentTimeMillis(),
        color = "0xFF599AC9",
        data = listOf()
    )
    val viewState = RoutineViewState.Success(
        links = Routine.Links(
            id = routine.id,
            title = routine.title,
            color = routine.color,
            time = routine.time,
            modifiedAt = routine.modifiedAt,
            links = listOf()
        ),
        full = routine,
    )
    PreviewTheme {
        SuccessTopAppBar(
            viewState = viewState,
        )
    }
}