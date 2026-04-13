package com.example.morningcalculator.shared.extensions

import java.util.Locale
import kotlin.time.Instant

fun Long.stringDateTime(
    locale: Locale = Locale.getDefault()
): String {
    val instant = Instant.fromEpochMilliseconds(this)
    return instant.stringDateTime(locale)

}