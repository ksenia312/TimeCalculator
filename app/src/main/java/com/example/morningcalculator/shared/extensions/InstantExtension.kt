package com.example.morningcalculator.shared.extensions

import android.content.Context
import kotlinx.datetime.toKotlinLocalDateTime
import java.util.Locale
import kotlin.time.Instant

fun Instant.stringDateTime(
    context: Context,
    locale: Locale = Locale.getDefault()
): String {
    val zone = deviceZoneId()
    return java.time.Instant
        .ofEpochMilli(this.toEpochMilliseconds())
        .atZone(zone)
        .toLocalDateTime()
        .toKotlinLocalDateTime()
        .stringDateTime(context, locale)
}

fun Instant.stringTime(): String {
    val zone = deviceZoneId()
    return java.time.Instant
        .ofEpochMilli(this.toEpochMilliseconds())
        .atZone(zone)
        .toLocalDateTime()
        .toKotlinLocalDateTime()
        .stringTime()
}

fun Instant.withZeroSeconds(): Instant {
    val millis = toEpochMilliseconds()
    val normalized = (millis / 60_000L) * 60_000L
    return Instant.fromEpochMilliseconds(normalized)
}