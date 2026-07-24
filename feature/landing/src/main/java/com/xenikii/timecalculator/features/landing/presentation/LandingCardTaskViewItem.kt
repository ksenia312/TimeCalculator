package com.xenikii.timecalculator.features.landing.presentation

import androidx.annotation.StringRes
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.domain.model.ScheduledTask
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
    task: ScheduledTask,
    now: Instant,
): LandingCardTaskViewItem {
    val startInstant = task.start
    val endInstant = task.end

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
        now <= startInstant -> task.duration
        now >= endInstant -> Duration.ZERO
        else -> endInstant - now
    }.coerceAtLeast(Duration.ZERO)

    return LandingCardTaskViewItem(
        title = task.title,
        headerRes = R.string.task_duration_left,
        remaining = remaining,
        start = startInstant,
        end = endInstant,
        progress = progress,
    )
}
