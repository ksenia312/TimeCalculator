package com.example.morningcalculator.features.landing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.features.home.ui.bottomIndent
import com.example.morningcalculator.features.landing.presentation.LandingState
import com.example.morningcalculator.shared.extensions.endAt
import com.example.morningcalculator.shared.extensions.isCompleted
import com.example.morningcalculator.shared.extensions.isOngoing
import com.example.morningcalculator.shared.extensions.startAtInstant
import com.example.morningcalculator.shared.extensions.stringDateTime
import com.example.morningcalculator.shared.extensions.stringTime
import com.example.morningcalculator.shared.extensions.whenToStart
import com.example.morningcalculator.shared.navigator.LocalNavHostController
import com.example.morningcalculator.shared.navigator.Screen
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewConstants
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingContent(
    viewState: LandingState,
    onEditRoutine: (Routine) -> Unit,
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
                        RoutinePager(
                            modifier = Modifier.weight(2f),
                            routines = routines,
                            onNavigate = onNavigate,
                            onEditRoutine = onEditRoutine,
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

@Composable
private fun RoutinePager(
    modifier: Modifier = Modifier,
    routines: List<Routine>,
    onNavigate: (routine: Routine) -> Unit,
    onEditRoutine: (Routine) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { routines.size })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            RoutineHeroCard(
                routine = routines[page],
                onNavigate = onNavigate,
                onEditRoutine = onEditRoutine,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(12.dp))

        PagerDots(
            count = routines.size,
            activeIndex = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun RoutineHeroCard(
    routine: Routine,
    onNavigate: (routine: Routine) -> Unit,
    onEditRoutine: (Routine) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOngoing = routine.isOngoing()
    val isCompleted = routine.isCompleted()

    val baseGradient = if (isOngoing) {
        LocalCustomColorScheme.current.accentDark
    } else {
        Color.Black
    }

    val statusDot = if (isOngoing) {
        LocalCustomColorScheme.current.success
    } else {
        LocalCustomColorScheme.current.unselected
    }

    val background = Brush.linearGradient(
        listOf(
            baseGradient,
            baseGradient.copy(alpha = 0.85f),
            baseGradient.copy(alpha = 0.72f),
        )
    )

    val startLabel = when {
        isOngoing || isCompleted -> "Started at"
        else -> "Will start"
    }
    val endLabel = when {
        isOngoing -> "Ends at"
        isCompleted -> "Completed at"
        else -> "Will end"
    }

    val startText = routine.whenToStart().stringDateTime()
    val endText = routine.endAt().stringDateTime()

    val now = rememberNow()
    val taskCount = routine.data.size

    val currentIndex = when {
        taskCount == 0 -> null
        isOngoing -> currentTaskIndex(routine, now)
        else -> 0
    }

    val nextIndex = when {
        taskCount == 0 -> null
        isOngoing -> currentIndex?.plus(1)
        else -> 1
    }?.takeIf { it in 0 until taskCount }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .clickable { onNavigate(routine) }
            .padding(24.dp, 32.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title, style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ), maxLines = 3, color = MaterialTheme.colorScheme.surface
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(statusDot, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isOngoing) "Running" else "Not running",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = startLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                )
                Text(
                    text = startText, style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ), color = MaterialTheme.colorScheme.surface
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = endLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                )
                Text(
                    text = endText, style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ), color = MaterialTheme.colorScheme.surface
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (currentIndex != null) {
            val current = routineTaskUI(routine = routine, index = currentIndex, now = now)

            TaskCard(
                header = if (isOngoing) "Current task" else "First task",
                title = current.title,
                start = current.start,
                end = current.end,
                progress = current.progress,
                isOngoing = isOngoing,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (nextIndex != null) {
            Spacer(Modifier.height(12.dp))

            val next = routineTaskUI(routine = routine, index = nextIndex, now = now)

            TaskCard(
                header = if (isOngoing) "Next task" else "Second task",
                title = next.title,
                start = next.start,
                end = next.end,
                progress = next.progress,
                isOngoing = isOngoing,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TaskCard(
    header: String,
    title: String,
    start: String,
    end: String,
    progress: Float,
    isOngoing: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardBg = if (isOngoing) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.16f)
    } else {
        Color.Transparent
    }

    val cardBorderColor = if (isOngoing) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.16f)
    }

    val textColor = MaterialTheme.colorScheme.surface
    val subTextColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(
                2.dp, cardBorderColor,
                RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = header, style = MaterialTheme.typography.labelMedium, color = subTextColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = title, style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ), color = textColor
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = start, style = MaterialTheme.typography.bodyMedium, color = subTextColor
            )
            Text(
                text = end, style = MaterialTheme.typography.bodyMedium, color = subTextColor
            )
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.20f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}

private data class RoutineTaskUI(
    val title: String,
    val start: String,
    val end: String,
    val progress: Float,
)

@Composable
private fun routineTaskUI(
    routine: Routine,
    index: Int,
    now: Instant,
): RoutineTaskUI {
    val link = routine.data[index]
    val title = link.task.title

    val startInstant = routine.startAtInstant().plus(durationUntilIndex(routine, index - 1))
    val endInstant = routine.startAtInstant().plus(durationUntilIndex(routine, index))

    val progress = when {
        now <= startInstant -> 0f
        now >= endInstant -> 1f
        else -> {
            val total = (endInstant - startInstant).inWholeMilliseconds.toFloat()
            if (total <= 0f) 0f
            else {
                val current = (now - startInstant).inWholeMilliseconds.toFloat()
                (current / total)
            }
        }
    }

    return RoutineTaskUI(
        title = title,
        start = startInstant.stringTime(),
        end = endInstant.stringTime(),
        progress = progress
    )
}

private fun durationUntilIndex(routine: Routine, index: Int): Duration {
    val safe = index.coerceAtLeast(-1)
    return routine.data.foldIndexed(Duration.ZERO) { currentIndex, acc, link ->
        if (currentIndex > safe) return@foldIndexed acc
        acc + taskDuration(link.task)
    }
}

private fun taskDuration(task: Task): Duration {
    return task.data.fold(Duration.ZERO) { acc, subData ->
        acc + subData.duration
    }
}

private fun currentTaskIndex(routine: Routine, now: Instant): Int? {
    val tasks = routine.data
    if (tasks.isEmpty()) return null

    val start = routine.startAtInstant()
    if (now <= start) return 0

    var acc = Duration.ZERO
    val elapsed = now - start

    tasks.forEachIndexed { index, link ->
        val d = taskDuration(link.task)
        val next = acc + d
        if (d > Duration.ZERO && elapsed < next) return index
        acc = next
    }

    return null
}

@Composable
private fun rememberNow(intervalMillis: Long = 1_000L): Instant {
    val now by produceState(
        initialValue = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    ) {
        while (true) {
            value = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            delay(intervalMillis)
        }
    }
    return now
}

@Composable
private fun PagerDots(
    count: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier,
) {
    val inactive = LocalCustomColorScheme.current.unselected
    val active = MaterialTheme.colorScheme.primary

    if (count <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val isActive = index == activeIndex
            Box(
                modifier = Modifier
                    .size(if (isActive) 8.dp else 6.dp)
                    .background(
                        color = if (isActive) active else inactive, shape = CircleShape
                    )
            )
        }
    }
}

@PreviewAll
@Composable
fun LandingContentPreview() {
    PreviewTheme {
        LandingContent(
            viewState = LandingState.Success(
                routines = PreviewConstants.routinesFull,
            ), onEditRoutine = {})
    }
}