package com.example.morningcalculator.shared.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri

private const val MAIN_ACTIVITY_CLASS_NAME = "com.example.morningcalculator.app.MainActivity"
private const val ROUTINE_SCHEME = "morningcalculator"
private const val ROUTINE_HOST = "routine"

fun Context.buildRoutineDeepLinkIntent(routineId: String): Intent {
    return Intent().apply {
        setClassName(packageName, MAIN_ACTIVITY_CLASS_NAME)
        action = Intent.ACTION_VIEW
        data = Uri.parse("$ROUTINE_SCHEME://$ROUTINE_HOST/$routineId")
        addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK
        )
    }
}
