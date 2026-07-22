package com.example.morningcalculator.data.auth

import com.example.morningcalculator.domain.repository.RoutineAlarmGateway
import com.example.morningcalculator.domain.repository.RoutineNotificationGateway
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.ScheduleRecordDataSource
import com.example.morningcalculator.domain.repository.TasksRepository
import kotlinx.coroutines.flow.first

/**
 * Wipes all local user data. Called on explicit logout and when a different user signs in on the
 * same device. Cancels alarms/notifications, clears the schedule registry and the Room database.
 */
class ClearLocalUserDataManager(
    private val tasksRepository: TasksRepository,
    private val routineRepository: RoutineRepository,
    private val alarmGateway: RoutineAlarmGateway,
    private val notificationGateway: RoutineNotificationGateway,
    private val scheduleRecordDataSource: ScheduleRecordDataSource,
) {
    suspend operator fun invoke() {
        scheduleRecordDataSource.trackedRoutineIds().forEach { routineId ->
            val taskCount = scheduleRecordDataSource.getRecord(routineId)?.taskCount ?: 0
            alarmGateway.cancelRoutine(routineId, taskCount)
            notificationGateway.cancelRoutineNotifications(routineId)
            notificationGateway.cancelProgress(routineId)
            scheduleRecordDataSource.removeRecord(routineId)
        }

        routineRepository.routinesFlow.first().forEach { routine ->
            routineRepository.deleteRoutine(routine.id)
        }
        tasksRepository.clearTasks()
    }
}