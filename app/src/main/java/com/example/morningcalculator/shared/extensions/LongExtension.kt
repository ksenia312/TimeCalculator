package com.example.morningcalculator.shared.extensions

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun Long.formatAsDateTime(overridePattern: String? = null): String {
    val zone = ZoneId.systemDefault()
    val instant = Instant.ofEpochMilli(this)
    val zonedDT = instant.atZone(zone)

    val now = ZonedDateTime.now(zone)

    val formatter = DateTimeFormatter.ofPattern(
        overridePattern ?: when {
            zonedDT.toLocalDate().isEqual(now.toLocalDate()) -> {
                "HH:mm"
            }

            zonedDT.year == now.year -> {
                "dd.MM HH:mm"
            }

            else -> {
                "dd.MM.yyyy HH:mm"
            }
        }
    )
    return zonedDT.format(formatter)
}