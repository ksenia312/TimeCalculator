package com.example.morningcalculator.shared.extensions

import android.content.Context
import com.example.morningcalculator.R
import kotlin.time.Duration

fun Duration.stringValue(context: Context): String {
    val totalSeconds = inWholeSeconds.coerceAtLeast(0)
    val separator = context.getString(R.string.duration_list_separator)

    // Less than an hour: show minutes AND seconds (no rounding up to whole minutes).
    if (totalSeconds < 3600) {
        val minutes = (totalSeconds / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()

        val parts = mutableListOf<String>()
        if (minutes > 0) {
            parts += context.resources.getQuantityString(
                R.plurals.duration_minutes, minutes, minutes
            )
        }
        // Always show seconds under an hour; keep them even when minutes are present
        // (e.g. "1 minute, 30 seconds"), but drop a trailing "0 seconds" like "2 minutes".
        if (seconds > 0 || parts.isEmpty()) {
            parts += context.resources.getQuantityString(
                R.plurals.duration_seconds, seconds, seconds
            )
        }
        return parts.joinToString(separator)
    }

    // One hour or more: days / hours / minutes (seconds are not useful at this scale).
    val totalMinutes = totalSeconds / 60
    val minutesInDay = 24L * 60L
    val days = totalMinutes / minutesInDay
    val hours = (totalMinutes % minutesInDay) / 60L
    val minutes = totalMinutes % 60L

    val parts = mutableListOf<String>()
    if (days > 0) parts += context.resources.getQuantityString(
        R.plurals.duration_days, days.toInt(), days.toInt()
    )
    if (hours > 0) parts += context.resources.getQuantityString(
        R.plurals.duration_hours, hours.toInt(), hours.toInt()
    )
    if (minutes > 0 || parts.isEmpty()) parts += context.resources.getQuantityString(
        R.plurals.duration_minutes, minutes.toInt(), minutes.toInt()
    )

    return parts.joinToString(separator)
}