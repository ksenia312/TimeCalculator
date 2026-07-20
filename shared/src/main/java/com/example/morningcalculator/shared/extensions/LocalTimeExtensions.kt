package com.example.morningcalculator.shared.extensions

import android.content.Context
import android.icu.text.DateTimePatternGenerator
import android.icu.text.RelativeDateTimeFormatter
import com.example.morningcalculator.R
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDateTime.stringTime(locale: Locale = Locale.getDefault()): String {
    return toJavaLocalDateTime().format(timeFormatter(locale))
}

fun LocalDateTime.stringDateTime(
    context: Context,
    locale: Locale = Locale.getDefault()
): String {
    val javaLdt = this.toJavaLocalDateTime()
    val today = LocalDate.now(deviceZoneId())
    val date = javaLdt.toLocalDate()

    val datePart = when (date) {
        today -> getRelativeDayName(context, locale, RelativeDateTimeFormatter.Direction.THIS)
        today.minusDays(1) -> getRelativeDayName(context, locale, RelativeDateTimeFormatter.Direction.LAST)
        today.plusDays(1) -> getRelativeDayName(context, locale, RelativeDateTimeFormatter.Direction.NEXT)
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

    return context.getString(R.string.datetime_with_comma, capitalizedDate, timePart)
}

private fun timeFormatter(locale: Locale): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("HH:mm", locale)
}

private fun getRelativeDayName(
    context: Context,
    locale: Locale,
    direction: RelativeDateTimeFormatter.Direction
): String {
    return when (direction) {
        RelativeDateTimeFormatter.Direction.LAST -> context.getString(R.string.relative_day_yesterday)
        RelativeDateTimeFormatter.Direction.NEXT -> context.getString(R.string.relative_day_tomorrow)
        RelativeDateTimeFormatter.Direction.THIS -> context.getString(R.string.relative_day_today)
        else -> RelativeDateTimeFormatter.getInstance(locale)
            .format(direction, RelativeDateTimeFormatter.AbsoluteUnit.DAY)
    }
}