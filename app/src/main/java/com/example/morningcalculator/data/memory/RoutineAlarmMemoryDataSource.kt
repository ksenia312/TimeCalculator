package com.example.morningcalculator.data.memory

import android.content.Context
import androidx.core.content.edit

class RoutineAlarmMemoryDataSource(context: Context) {

    private val prefs = context.getSharedPreferences("routine_alarms", Context.MODE_PRIVATE)

    fun setStepCount(routineId: String, stepCount: Int) {
        prefs.edit { putInt(stepKey(routineId), stepCount) }
    }

    fun getStepCount(routineId: String): Int? {
        val value = prefs.getInt(stepKey(routineId), -1)
        return if (value == -1) null else value
    }

    fun clear(routineId: String) {
        prefs.edit {
            remove(stepKey(routineId))
        }
    }

    private fun stepKey(routineId: String): String {
        return "steps_$routineId"
    }
}