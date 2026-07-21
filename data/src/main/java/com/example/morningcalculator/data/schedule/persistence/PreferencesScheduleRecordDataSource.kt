package com.example.morningcalculator.data.schedule.persistence

import android.content.Context
import com.example.morningcalculator.domain.model.ScheduleRecord
import com.example.morningcalculator.domain.repository.ScheduleRecordDataSource

class PreferencesScheduleRecordDataSource(context: Context) : ScheduleRecordDataSource {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getRecord(routineId: String): ScheduleRecord? {
        val signature = prefs.getString(recordSignatureKey(routineId), null) ?: return null
        val taskCount = prefs.getInt(recordTaskCountKey(routineId), -1)
        if (taskCount < 0) return null
        return ScheduleRecord(signature = signature, taskCount = taskCount)
    }

    override fun putRecord(routineId: String, record: ScheduleRecord) {
        prefs.edit()
            .putString(recordSignatureKey(routineId), record.signature)
            .putInt(recordTaskCountKey(routineId), record.taskCount)
            .putStringSet(
                trackedRoutineIdsKey,
                trackedRoutineIds().plus(routineId).toSet(),
            )
            .apply()
    }

    override fun removeRecord(routineId: String) {
        prefs.edit()
            .remove(recordSignatureKey(routineId))
            .remove(recordTaskCountKey(routineId))
            .putStringSet(
                trackedRoutineIdsKey,
                trackedRoutineIds().minus(routineId).toSet(),
            )
            .apply()
    }

    override fun trackedRoutineIds(): Set<String> {
        return prefs.getStringSet(trackedRoutineIdsKey, emptySet()).orEmpty().toSet()
    }

    private fun recordSignatureKey(routineId: String) = "routine_signature_$routineId"

    private fun recordTaskCountKey(routineId: String) = "routine_task_count_$routineId"

    companion object {
        private const val PREFS_NAME = "routine_schedule_registry"
        private const val trackedRoutineIdsKey = "tracked_routine_ids"
    }
}
