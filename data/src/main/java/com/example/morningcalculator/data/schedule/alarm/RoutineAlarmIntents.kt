package com.example.morningcalculator.data.schedule.alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.morningcalculator.domain.model.RoutineAlarmKind

internal const val ACTION_ROUTINE_ALARM = "com.example.morningcalculator.action.ROUTINE_ALARM"
const val EXTRA_ROUTINE_ID = "extra_routine_id"
internal const val EXTRA_ALARM_KIND = "extra_alarm_kind"
internal const val EXTRA_BOUNDARY_INDEX = "extra_boundary_index"
internal const val EXTRA_TRIGGER_AT_MILLIS = "extra_trigger_at_millis"

private const val MAIN_ACTIVITY_CLASS_NAME = "com.example.morningcalculator.app.MainActivity"
private const val ROUTINE_SCHEME = "morningcalculator"

fun stableAlarmRequestCode(
    routineId: String,
    kind: RoutineAlarmKind,
    boundaryIndex: Int,
): Int = stableCode(routineId, kind.name, boundaryIndex.toString())

fun stableNotificationId(
    routineId: String,
    suffix: String,
): Int = stableCode(routineId, suffix)

fun buildRoutineAlarmIntent(
    context: Context,
    routineId: String,
    kind: RoutineAlarmKind,
    boundaryIndex: Int,
    triggerAtMillis: Long,
): Intent {
    return Intent(context, com.example.morningcalculator.data.schedule.receiver.RoutineAlarmReceiver::class.java).apply {
        action = ACTION_ROUTINE_ALARM
        data = Uri.parse("$ROUTINE_SCHEME://alarm/$routineId/${kind.name.lowercase()}/$boundaryIndex")
        putExtra(EXTRA_ROUTINE_ID, routineId)
        putExtra(EXTRA_ALARM_KIND, kind.name)
        putExtra(EXTRA_BOUNDARY_INDEX, boundaryIndex)
        putExtra(EXTRA_TRIGGER_AT_MILLIS, triggerAtMillis)
    }
}

fun buildRoutineAlarmPendingIntent(
    context: Context,
    routineId: String,
    kind: RoutineAlarmKind,
    boundaryIndex: Int,
    triggerAtMillis: Long,
): PendingIntent {
    val intent = buildRoutineAlarmIntent(context, routineId, kind, boundaryIndex, triggerAtMillis)
    val requestCode = stableAlarmRequestCode(routineId, kind, boundaryIndex)
    return PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

fun buildRoutineDetailIntent(
    context: Context,
    routineId: String,
): Intent {
    return Intent().apply {
        setClassName(context.packageName, MAIN_ACTIVITY_CLASS_NAME)
        action = Intent.ACTION_VIEW
        data = Uri.parse("$ROUTINE_SCHEME://routine/$routineId")
        putExtra(EXTRA_ROUTINE_ID, routineId)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

fun buildRoutineDetailPendingIntent(
    context: Context,
    routineId: String,
): PendingIntent {
    val intent = buildRoutineDetailIntent(context, routineId)
    return PendingIntent.getActivity(
        context,
        stableCode(routineId, "open"),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun stableCode(vararg parts: String): Int {
    return parts.joinToString("|").hashCode() and 0x7fffffff
}
