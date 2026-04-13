package com.example.morningcalculator.shared.extensions

import kotlinx.datetime.toKotlinLocalDateTime
import java.time.ZoneId
import java.util.Locale
import kotlin.time.Instant

fun Instant.stringDateTime(
    locale: Locale = Locale.getDefault()
): String {
    val zone = ZoneId.systemDefault()
    return java.time.Instant
        .ofEpochMilli(this.toEpochMilliseconds())
        .atZone(zone)
        .toLocalDateTime()
        .toKotlinLocalDateTime()
        .stringDateTime(locale)
}