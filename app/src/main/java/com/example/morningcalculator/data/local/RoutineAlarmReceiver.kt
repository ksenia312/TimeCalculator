package com.example.morningcalculator.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.morningcalculator.app.notifications.RoutineNotificationPublisher
import com.example.morningcalculator.data.db.RoutinesDao
import com.example.morningcalculator.data.mapper.toDomain
import com.example.morningcalculator.data.memory.RoutineAlarmMemoryDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RoutineAlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val routinesDao: RoutinesDao by inject()
    private val memoryDataSource: RoutineAlarmMemoryDataSource by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val routineId = intent.getStringExtra(EXTRA_ROUTINE_ID) ?: return
        val stepIndex = intent.getIntExtra(EXTRA_STEP_INDEX, -1)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (stepIndex < 0) return@launch

                val populated = routinesDao.getRoutinePopulated(routineId)
                val routine = populated?.toDomain()

                if (routine == null) {
                    RoutineNotificationPublisher.dismiss(context, routineId)
                    memoryDataSource.clear(routineId)
                    return@launch
                }

                if (stepIndex >= routine.data.size) {
                    RoutineNotificationPublisher.dismiss(context, routineId)
                    memoryDataSource.clear(routineId)
                    return@launch
                }

                RoutineNotificationPublisher.showTask(
                    context = context,
                    routine = routine,
                    taskIndex = stepIndex,
                    shouldAlert = true
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ROUTINE_ID = "extra_routine_id"
        const val EXTRA_STEP_INDEX = "extra_step_index"
    }
}