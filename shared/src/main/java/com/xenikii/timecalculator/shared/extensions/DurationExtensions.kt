package com.xenikii.timecalculator.shared.extensions

import android.content.Context
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import com.xenikii.timecalculator.R
import java.util.Locale
import kotlin.time.Duration

/**
 * Locale-aware short representation of a duration, mirroring the compact style of
 * [Duration.toString] (e.g. "1h 30m") but adapted to the current locale via ICU
 * (e.g. "1 ч 30 мин" in Russian). Uses NARROW units and locale-correct spacing/order.
 */
fun Duration.shortStringValue(locale: Locale = Locale.getDefault()): String {
    val totalSeconds = inWholeSeconds.coerceAtLeast(0)
    val measures = mutableListOf<Measure>()

    if (totalSeconds < 3600) {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        if (minutes > 0) measures += Measure(minutes, MeasureUnit.MINUTE)
        if (seconds > 0 || measures.isEmpty()) measures += Measure(seconds, MeasureUnit.SECOND)
    } else {
        val totalMinutes = totalSeconds / 60
        val minutesInDay = 24L * 60L
        val days = totalMinutes / minutesInDay
        val hours = (totalMinutes % minutesInDay) / 60L
        val minutes = totalMinutes % 60L
        if (days > 0) measures += Measure(days, MeasureUnit.DAY)
        if (hours > 0) measures += Measure(hours, MeasureUnit.HOUR)
        if (minutes > 0 || measures.isEmpty()) measures += Measure(minutes, MeasureUnit.MINUTE)
    }

    return MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.NARROW)
        .formatMeasures(*measures.toTypedArray())
}

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