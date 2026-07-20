package com.example.morningcalculator.features.landing.presentation

import androidx.annotation.StringRes
import com.example.morningcalculator.R
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.features.landing.ui.linkDuration
import com.example.morningcalculator.shared.extensions.startAtInstant
import kotlin.time.Duration
import kotlin.time.Instant

data class LandingCardTaskViewItem(
    val title: String,
    @param:StringRes val headerRes: Int,
    val remaining: Duration,
    val start: Instant,
    val end: Instant,
    val progress: Float,
)

fun createLandingCardTaskViewItem(
    routine: Routine,
    index: Int,
    now: Instant,
): LandingCardTaskViewItem {
    val link = routine.data[index]
    val title = link.task.title

    val startOffset = durationUntilIndex(routine, index - 1)
    val startInstant = routine.startAtInstant() + startOffset
    val duration = linkDuration(link).coerceAtLeast(Duration.ZERO)
    val endInstant = startInstant + duration

    val progress = when {
        now <= startInstant -> 0f
        now >= endInstant -> 1f
        else -> {
            val total = (endInstant - startInstant).inWholeMilliseconds.toFloat()
            if (total <= 0f) 0f
            else (now - startInstant).inWholeMilliseconds.toFloat() / total
        }
    }

    val remaining = when {
        now <= startInstant -> duration
        now >= endInstant -> Duration.ZERO
        else -> endInstant - now
    }.coerceAtLeast(Duration.ZERO)

    return LandingCardTaskViewItem(
        title = title,
        headerRes = R.string.task_duration_left,
        remaining = remaining,
        start = startInstant,
        end = endInstant,
        progress = progress,
    )
}

private fun durationUntilIndex(routine: Routine, index: Int): Duration {
    if (index < 0) return Duration.ZERO
    if (routine.data.isEmpty()) return Duration.ZERO
    val last = index.coerceAtMost(routine.data.lastIndex)
    var acc = Duration.ZERO
    for (i in 0..last) {
        acc += linkDuration(routine.data[i]).coerceAtLeast(Duration.ZERO)
    }
    return acc
}
