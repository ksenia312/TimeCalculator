package com.example.morningcalculator.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.morningcalculator.app.notifications.RoutineNotificationPublisher
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.data.db.RoutinesDao
import com.example.morningcalculator.data.mapper.toDomain
import com.example.morningcalculator.shared.extensions.getCurrentTaskIndex
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RoutineNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val routinesDao: RoutinesDao by inject()
    override suspend fun doWork(): Result {
        return try {
            val routines = routinesDao.getRoutinesPopulated().firstOrNull() ?: return Result.success()
            routines.forEach { populated ->
                val routine = populated.toDomain()
                showNotificationIfActive(routine)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotificationIfActive(routine: Routine) {
        if (routine.data.isEmpty()) return

        val currentTaskIndex = routine.getCurrentTaskIndex()
        if (currentTaskIndex != null) {
            RoutineNotificationPublisher.showTask(
                context = applicationContext,
                routine = routine,
                taskIndex = currentTaskIndex
            )
        } else {
            RoutineNotificationPublisher.dismiss(applicationContext, routine.id)
        }
    }
}
