package com.example.morningcalculator.features.landing.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.features.home.ui.bottomIndent
import com.example.morningcalculator.features.landing.presentation.LandingState
import com.example.morningcalculator.features.landing.ui.card.LandingCardPager
import com.example.morningcalculator.shared.extensions.endAtInstant
import com.example.morningcalculator.shared.extensions.startAtInstant
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import com.example.morningcalculator.shared.navigator.Screen
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewConstants
import com.example.morningcalculator.shared.preview.PreviewTheme
import kotlin.time.Duration
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingContent(
    viewState: LandingState,
) {
    val navigator = LocalNavHostController.current
    val onNavigate: (routine: Routine) -> Unit = {
        navigator.navigate(Screen.Routine.route)
        navigator.currentBackStackEntry?.savedStateHandle?.set("routineId", it.id)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (viewState) {
            is LandingState.Loading -> {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .bottomIndent()
                )
            }

            is LandingState.Success -> {
                val routines = viewState.routines
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(Modifier.height(16.dp))

                    if (routines.isEmpty()) {
                        Text(
                            text = "No routines scheduled",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LandingCardPager(
                            modifier = Modifier.weight(2f),
                            routines = routines,
                            onNavigate = onNavigate,
                        )
                    }

                    Spacer(
                        Modifier
                            .weight(1f)
                            .bottomIndent()
                    )
                }
            }

            is LandingState.Error -> {
                Text(
                    text = viewState.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .bottomIndent()
                )
            }
        }
    }
}

fun currentTaskIndex(routine: Routine, now: Instant): Int? {
    val tasks = routine.data
    if (tasks.isEmpty()) return null

    val start = routine.startAtInstant()
    val end = routine.endAtInstant()

    if (now <= start) return 0
    if (now >= end) return tasks.lastIndex

    var cursor = start

    tasks.forEachIndexed { index, link ->
        val d = linkDuration(link).coerceAtLeast(Duration.ZERO)
        val next = cursor + d

        if (d == Duration.ZERO) {
            if (now == cursor) return index
        } else {
            if (now < next) return index
        }

        cursor = next
    }

    return tasks.lastIndex
}

fun linkDuration(link: RoutineLink): Duration {
    return link.subData?.duration ?: link.task.data.fold(Duration.ZERO) { acc, subData ->
        acc + subData.duration
    }
}


@PreviewAll
@Composable
fun LandingContentPreview() {
    PreviewTheme {
        LandingContent(
            viewState = LandingState.Success(
                routines = PreviewConstants.routinesFull,
            )
        )
    }
}