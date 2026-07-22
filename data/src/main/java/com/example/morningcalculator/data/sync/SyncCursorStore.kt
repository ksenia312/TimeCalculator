package com.example.morningcalculator.data.sync

import android.content.Context

class SyncCursorStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTasksCursor(): SyncCursor? = decodeCursor(prefs.getString(KEY_TASKS_UPDATED_AT, null))

    fun setTasksCursor(value: SyncCursor?) {
        prefs.edit().putString(KEY_TASKS_UPDATED_AT, encodeCursor(value)).apply()
    }

    fun getRoutinesCursor(): SyncCursor? =
        decodeCursor(prefs.getString(KEY_ROUTINES_UPDATED_AT, null))

    fun setRoutinesCursor(value: SyncCursor?) {
        prefs.edit().putString(KEY_ROUTINES_UPDATED_AT, encodeCursor(value)).apply()
    }

    fun reset() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val CURSOR_DELIMITER = "|"
        const val PREFS_NAME = "sync_cursor"
        const val KEY_TASKS_UPDATED_AT = "tasks_updated_at"
        const val KEY_ROUTINES_UPDATED_AT = "routines_updated_at"
    }

    private fun decodeCursor(raw: String?): SyncCursor? {
        if (raw.isNullOrBlank()) return null
        if (!raw.contains(CURSOR_DELIMITER)) {
            return SyncCursor(updatedAt = raw, lastEntityId = null)
        }
        val (updatedAt, entityIdRaw) = raw.split(CURSOR_DELIMITER, limit = 2)
        if (updatedAt.isBlank()) return null
        return SyncCursor(
            updatedAt = updatedAt,
            lastEntityId = entityIdRaw.ifBlank { null },
        )
    }

    private fun encodeCursor(cursor: SyncCursor?): String? = cursor?.let {
        "${it.updatedAt}$CURSOR_DELIMITER${it.lastEntityId.orEmpty()}"
    }
}
