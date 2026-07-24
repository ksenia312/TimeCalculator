package com.xenikii.timecalculator.shared.extensions

import android.content.Context
import java.util.Locale
import kotlin.time.Instant

fun Long.stringDateTime(
    context: Context,
    locale: Locale = Locale.getDefault()
): String {
    val instant = Instant.fromEpochMilliseconds(this)
    return instant.stringDateTime(context, locale)

}