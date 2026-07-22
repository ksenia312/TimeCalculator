package com.example.morningcalculator.data.auth

import com.example.morningcalculator.data.db.RoutinesDao
import com.example.morningcalculator.data.db.SyncDao
import com.example.morningcalculator.data.db.TasksDao
import com.example.morningcalculator.data.sync.SyncCursorStore
import com.example.morningcalculator.domain.repository.RoutineAlarmGateway
import com.example.morningcalculator.domain.repository.RoutineNotificationGateway
import com.example.morningcalculator.domain.repository.ScheduleRecordDataSource

/**
 * Wipes all local user data. Called on explicit logout and when a different user signs in on the
 * same device. Cancels alarms/notifications, clears the schedule registry and the Room database.
 */
class ClearLocalUserDataManager(
    private val tasksDao: TasksDao,
    private val routinesDao: RoutinesDao,
    private val syncDao: SyncDao,
    private val cursorStore: SyncCursorStore,
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

        routinesDao.clearRoutines()
        tasksDao.clearTasks()
        syncDao.clearAllPendingDeletions()
        cursorStore.reset()
    }
}