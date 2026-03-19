package com.example.morningcalculator.shared.extensions

import com.example.morningcalculator.core.model.Routine
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import java.time.ZoneId

fun Routine.endAt(): LocalDateTime {
    val endMillis = scheduledAt.toEpochMilliseconds()
    return java.time.Instant
        .ofEpochMilli(endMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .toKotlinLocalDateTime()
}

fun Routine.whenToStart(): LocalDateTime {
    val total: Duration = data.fold(Duration.ZERO) { acc, link ->
        acc + (link.subData?.duration ?: Duration.ZERO)
    }

    val endMillis = scheduledAt.toEpochMilliseconds()
    val end = java.time.Instant
        .ofEpochMilli(endMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    return end.minus(total.toJavaDuration()).toKotlinLocalDateTime()
}

fun Routine.timeOnMoment(index: Int): LocalDateTime {
    val total: Duration = data.foldIndexed(Duration.ZERO) { currentIndex, acc, link ->
        if (currentIndex <= index) return@foldIndexed acc
        acc + (link.subData?.duration ?: Duration.ZERO)
    }

    val endMillis = scheduledAt.toEpochMilliseconds()
    val end = java.time.Instant
        .ofEpochMilli(endMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    return end.minus(total.toJavaDuration()).toKotlinLocalDateTime()
}