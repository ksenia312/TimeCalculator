package com.example.morningcalculator.app.schedule

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.util.concurrent.atomic.AtomicBoolean

class RoutineExactAlarmPermissionRequester(
    private val context: Context,
) {
    private val prompted = AtomicBoolean(false)

    fun promptIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        if (prompted.get()) return

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) == null) return
        if (!prompted.compareAndSet(false, true)) return
        context.startActivity(intent)
    }
}
