package com.xenikii.timecalculator.data.schedule.persistence

import android.content.Context
import androidx.core.content.edit
import com.xenikii.timecalculator.domain.model.NotificationMode
import com.xenikii.timecalculator.domain.model.ScheduleRecord
import com.xenikii.timecalculator.domain.repository.ScheduleRecordDataSource

class PreferencesScheduleRecordDataSource(context: Context) : ScheduleRecordDataSource {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getRecord(routineId: String): ScheduleRecord? {
        val signature = prefs.getString(recordSignatureKey(routineId), null) ?: return null
        val taskCount = prefs.getInt(recordTaskCountKey(routineId), -1)
        if (taskCount < 0) return null
        val notificationMode = prefs.getString(recordNotificationModeKey(routineId), null)
            ?.let {
                runCatching { NotificationMode.valueOf(it) }.getOrNull()
            }
        return ScheduleRecord(signature = signature, taskCount = taskCount, notificationMode = notificationMode)
    }

    override fun putRecord(routineId: String, record: ScheduleRecord) {
        prefs.edit {
            putString(recordSignatureKey(routineId), record.signature)
                .putInt(recordTaskCountKey(routineId), record.taskCount)
                .apply {
                    val notificationMode = record.notificationMode
                    if (notificationMode != null) {
                        putString(recordNotificationModeKey(routineId), notificationMode.name)
                    } else {
                        remove(recordNotificationModeKey(routineId))
                    }
                }
                .putStringSet(
                    TRACKED_ROUTINE_IDS_KEY,
                    trackedRoutineIds().plus(routineId).toSet(),
                )
        }
    }

    override fun removeRecord(routineId: String) {
        prefs.edit {
            remove(recordSignatureKey(routineId))
                .remove(recordTaskCountKey(routineId))
                .remove(recordNotificationModeKey(routineId))
                .putStringSet(
                    TRACKED_ROUTINE_IDS_KEY,
                    trackedRoutineIds().minus(routineId).toSet(),
                )
        }
    }

    override fun trackedRoutineIds(): Set<String> {
        return prefs.getStringSet(TRACKED_ROUTINE_IDS_KEY, emptySet()).orEmpty().toSet()
    }

    private fun recordSignatureKey(routineId: String) = "routine_signature_$routineId"

    private fun recordTaskCountKey(routineId: String) = "routine_task_count_$routineId"

    private fun recordNotificationModeKey(routineId: String) = "routine_notification_mode_$routineId"

    companion object {
        private const val PREFS_NAME = "routine_schedule_registry"
        private const val TRACKED_ROUTINE_IDS_KEY = "tracked_routine_ids"
    }
}
