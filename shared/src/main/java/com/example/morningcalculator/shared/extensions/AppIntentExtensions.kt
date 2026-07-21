package com.example.morningcalculator.shared.extensions

import android.content.Intent
import com.example.morningcalculator.shared.navigator.AppRoute

fun Intent.toAppRoute(): AppRoute? {
    val routineId = getStringExtra("extra_routine_id") ?: return null
    return AppRoute.Routine(routineId)
}
