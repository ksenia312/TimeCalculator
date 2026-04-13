package com.example.morningcalculator.shared.extensions

import android.icu.text.DateTimePatternGenerator
import android.icu.text.RelativeDateTimeFormatter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDateTime.stringTime(locale: Locale = Locale.getDefault()): String {
    return toJavaLocalDateTime().format(timeFormatter(locale))
}

fun LocalDateTime.stringDateTime(locale: Locale = Locale.getDefault()): String {
    val javaLdt = this.toJavaLocalDateTime()
    val today = LocalDate.now(deviceZoneId())
    val date = javaLdt.toLocalDate()

    val datePart = when (date) {
        today -> getRelativeDayName(locale, RelativeDateTimeFormatter.Direction.THIS)
        today.minusDays(1) -> getRelativeDayName(locale, RelativeDateTimeFormatter.Direction.LAST)
        today.plusDays(1) -> getRelativeDayName(locale, RelativeDateTimeFormatter.Direction.NEXT)
        else -> {
            val skeleton = if (date.year == today.year) "MMMMd" else "yyyyMMMMd"
            val pattern = DateTimePatternGenerator.getInstance(locale).getBestPattern(skeleton)
            javaLdt.format(DateTimeFormatter.ofPattern(pattern, locale))
        }
    }

    val timePart = javaLdt.format(timeFormatter(locale))
    val capitalizedDate = datePart.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
    }

    return "$capitalizedDate, $timePart"
}

private fun timeFormatter(locale: Locale): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("HH:mm", locale)
}

private fun getRelativeDayName(
    locale: Locale,
    direction: RelativeDateTimeFormatter.Direction
): String {
    return try {
        RelativeDateTimeFormatter.getInstance(locale)
            .format(direction, RelativeDateTimeFormatter.AbsoluteUnit.DAY)
    } catch (e: Exception) {
        when (direction) {
            RelativeDateTimeFormatter.Direction.LAST -> "Yesterday"
            RelativeDateTimeFormatter.Direction.NEXT -> "Tomorrow"
            else -> "Today"
        }
    }
}