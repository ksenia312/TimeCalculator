package com.example.morningcalculator.shared.extensions

import kotlin.time.Duration

fun Duration.stringValue(): String {
    val totalSeconds = inWholeSeconds.coerceAtLeast(0)
    val totalMinutes = ((totalSeconds + 59) / 60).coerceAtLeast(0)

    if (totalMinutes <= 60) {
        return "$totalMinutes ${pluralize(totalMinutes, "minute")}"
    }

    val minutesInDay = 24L * 60L
    val days = totalMinutes / minutesInDay
    val hours = (totalMinutes % minutesInDay) / 60L
    val minutes = totalMinutes % 60L

    val parts = mutableListOf<String>()
    if (days > 0) parts += "$days ${pluralize(days, "day")}"
    if (hours > 0) parts += "$hours ${pluralize(hours, "hour")}"
    if (minutes > 0 || parts.isEmpty()) {
        parts += "$minutes ${pluralize(minutes, "minute")}"
    }

    return parts.joinToString(", ")
}

private fun pluralize(value: Long, singular: String): String {
    return if (value == 1L) singular else "${singular}s"
}