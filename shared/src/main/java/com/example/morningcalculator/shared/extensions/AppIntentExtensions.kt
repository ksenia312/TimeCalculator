package com.example.morningcalculator.shared.extensions

import android.content.Intent
import com.example.morningcalculator.shared.navigator.AppRoute

fun Intent.toAppRoute(): AppRoute? {
    val data = data ?: return null
    if (data.scheme != ROUTINE_SCHEME || data.host != ROUTINE_HOST) return null

    val routineId = data.lastPathSegment?.takeIf { it.isNotBlank() } ?: return null
    return AppRoute.Routine(routineId)
}

private const val ROUTINE_SCHEME = "morningcalculator"
private const val ROUTINE_HOST = "routine"
