package com.example.morningcalculator.shared.extensions

import android.content.Context
import com.example.morningcalculator.R
import kotlin.time.Duration

fun Duration.stringValue(context: Context): String {
    val totalSeconds = inWholeSeconds.coerceAtLeast(0)
    val totalMinutes = ((totalSeconds + 59) / 60).coerceAtLeast(0)

    if (totalMinutes <= 60) {
        return context.resources.getQuantityString(
            R.plurals.duration_minutes,
            totalMinutes.toInt(),
            totalMinutes.toInt()
        )
    }

    val minutesInDay = 24L * 60L
    val days = totalMinutes / minutesInDay
    val hours = (totalMinutes % minutesInDay) / 60L
    val minutes = totalMinutes % 60L

    val parts = mutableListOf<String>()
    if (days > 0) parts += context.resources.getQuantityString(
        R.plurals.duration_days,
        days.toInt(),
        days.toInt()
    )
    if (hours > 0) parts += context.resources.getQuantityString(
        R.plurals.duration_hours,
        hours.toInt(),
        hours.toInt()
    )
    if (minutes > 0 || parts.isEmpty()) {
        parts += context.resources.getQuantityString(
            R.plurals.duration_minutes,
            minutes.toInt(),
            minutes.toInt()
        )
    }

    return parts.joinToString(context.getString(R.string.duration_list_separator))
}