package com.example.morningcalculator.shared.extensions

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Long.formatAsDateTime(
    overridePattern: String? = null,
    locale: Locale = Locale.getDefault()
): String {
    val zone = ZoneId.systemDefault()
    val instant = Instant.ofEpochMilli(this)
    val zonedDT = instant.atZone(zone)
    val now = ZonedDateTime.now(zone)

    if (zonedDT.toLocalDate().isEqual(now.toLocalDate())) {
        return "Today"
    }

    if (zonedDT.toLocalDate().isEqual(now.toLocalDate().minusDays(1))) {
        return "Yesterday"
    }

    val pattern = overridePattern ?: when {
        zonedDT.year == now.year -> {
            "d MMM"
        }

        else -> {
            "d MMMM yyyy"
        }
    }

    val formatter = DateTimeFormatter.ofPattern(pattern, locale)
    return zonedDT.format(formatter)
}