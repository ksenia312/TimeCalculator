package com.xenikii.timecalculator.data.sync

import com.xenikii.timecalculator.data.db.RoutinesDao
import com.xenikii.timecalculator.data.db.SyncDao
import com.xenikii.timecalculator.data.db.TasksDao
import com.xenikii.timecalculator.data.sync.remote.RemoteRoutine
import com.xenikii.timecalculator.data.sync.remote.RemoteTask
import com.xenikii.timecalculator.data.sync.remote.SupabaseRemoteDataSource
import com.xenikii.timecalculator.data.sync.remote.toEntities
import com.xenikii.timecalculator.data.sync.remote.toRemote
import com.xenikii.timecalculator.data.sync.remote.tombstoneRoutine
import com.xenikii.timecalculator.data.sync.remote.tombstoneTask
import com.xenikii.timecalculator.domain.repository.AuthRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncEngine(
    private val tasksDao: TasksDao,
    private val routinesDao: RoutinesDao,
    private val syncDao: SyncDao,
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource,
    private val syncCursorStore: SyncCursorStore,
    private val authRepository: AuthRepository,
) {
    private val mutex = Mutex()

    suspend fun sync(): Result<Unit> = mutex.withLock {
        if (!authRepository.hasActiveSession()) return@withLock Result.success(Unit)
        runCatching {
            pullInto()
            pushDeletions()
            pushUpserts()
        }
    }

    suspend fun pushLocalChanges(): Result<Unit> = mutex.withLock {
        if (!authRepository.hasActiveSession()) return@withLock Result.success(Unit)
        runCatching {
            pullInto()
            pushDeletions()
            pushUpserts()
        }
    }

    private suspend fun pushDeletions() {
        val deletions = syncDao.getPendingDeletions()
        if (deletions.isEmpty()) return

        val (taskDeletions, routineDeletions) = deletions.partition { it.entityType == TYPE_TASK }
        if (taskDeletions.isNotEmpty()) {
            val staleTaskDeletionIds = mutableListOf<String>()
            val taskTombstones = mutableListOf<RemoteTask>()
            for (deletion in taskDeletions) {
                val localTaskModifiedAt = tasksDao.getTaskById(deletion.id)?.modifiedAt
                if (localTaskModifiedAt != null && localTaskModifiedAt > deletion.modifiedAt) {
                    staleTaskDeletionIds += deletion.id
                } else {
                    taskTombstones += tombstoneTask(deletion.id, deletion.modifiedAt)
                }
            }

            if (taskTombstones.isNotEmpty()) {
                supabaseRemoteDataSource.pushTasks(taskTombstones)
            }

            val clearedIds = staleTaskDeletionIds + taskTombstones.map { it.id }
            if (clearedIds.isNotEmpty()) {
                syncDao.clearPendingDeletions(TYPE_TASK, clearedIds)
            }
        }
        if (routineDeletions.isNotEmpty()) {
            val staleRoutineDeletionIds = mutableListOf<String>()
            val routineTombstones = mutableListOf<RemoteRoutine>()
            for (deletion in routineDeletions) {
                val localRoutineModifiedAt = routinesDao.getRoutineById(deletion.id)?.modifiedAt
                if (localRoutineModifiedAt != null && localRoutineModifiedAt > deletion.modifiedAt) {
                    staleRoutineDeletionIds += deletion.id
                } else {
                    routineTombstones += tombstoneRoutine(deletion.id, deletion.modifiedAt)
                }
            }

            if (routineTombstones.isNotEmpty()) {
                supabaseRemoteDataSource.pushRoutines(routineTombstones)
            }

            val clearedIds = staleRoutineDeletionIds + routineTombstones.map { it.id }
            if (clearedIds.isNotEmpty()) {
                syncDao.clearPendingDeletions(TYPE_ROUTINE, clearedIds)
            }
        }
    }

    private suspend fun pushUpserts() {
        val pendingTasks = tasksDao.getPendingTasks()
        if (pendingTasks.isNotEmpty()) {
            supabaseRemoteDataSource.pushTasks(pendingTasks.map { it.toRemote() })
            tasksDao.clearTasksPending(pendingTasks.map { it.task.id })
        }

        val pendingRoutines = routinesDao.getPendingRoutines()
        if (pendingRoutines.isNotEmpty()) {
            supabaseRemoteDataSource.pushRoutines(pendingRoutines.map { it.toRemote() })
            routinesDao.clearRoutinesPending(pendingRoutines.map { it.routine.id })
        }
    }

    private suspend fun pullInto() {
        val initialTaskCursor = syncCursorStore.getTasksCursor()
        var taskOffset = 0
        var latestTaskCursor = initialTaskCursor
        while (true) {
            val remoteTasks = supabaseRemoteDataSource.pullTasks(
                updatedAtCursor = initialTaskCursor?.updatedAt,
                limit = PAGE_SIZE,
                offset = taskOffset,
            )
            if (remoteTasks.isEmpty()) break

            for (remoteTask in remoteTasks) {
                if (!isAfterCursor(remoteTask.updatedAt, remoteTask.id, initialTaskCursor)) continue
                applyRemoteTask(remoteTask)
                val updatedAt = remoteTask.updatedAt ?: continue
                latestTaskCursor = SyncCursor(updatedAt = updatedAt, lastEntityId = remoteTask.id)
            }

            if (remoteTasks.size < PAGE_SIZE) break
            taskOffset += PAGE_SIZE
        }
        if (latestTaskCursor != initialTaskCursor) {
            syncCursorStore.setTasksCursor(latestTaskCursor)
        }

        val initialRoutineCursor = syncCursorStore.getRoutinesCursor()
        var routineOffset = 0
        var latestRoutineCursor = initialRoutineCursor
        while (true) {
            val remoteRoutines = supabaseRemoteDataSource.pullRoutines(
                updatedAtCursor = initialRoutineCursor?.updatedAt,
                limit = PAGE_SIZE,
                offset = routineOffset,
            )
            if (remoteRoutines.isEmpty()) break

            for (remoteRoutine in remoteRoutines) {
                if (!isAfterCursor(remoteRoutine.updatedAt, remoteRoutine.id, initialRoutineCursor)) continue
                applyRemoteRoutine(remoteRoutine)
                val updatedAt = remoteRoutine.updatedAt ?: continue
                latestRoutineCursor = SyncCursor(updatedAt = updatedAt, lastEntityId = remoteRoutine.id)
            }

            if (remoteRoutines.size < PAGE_SIZE) break
            routineOffset += PAGE_SIZE
        }
        if (latestRoutineCursor != initialRoutineCursor) {
            syncCursorStore.setRoutinesCursor(latestRoutineCursor)
        }
    }

    private suspend fun applyRemoteTask(remoteTask: RemoteTask) {
        val localTask = tasksDao.getTaskById(remoteTask.id)
        val localModifiedAt = localTask?.modifiedAt ?: 0L
        when {
            remoteTask.deleted -> {
                if (localTask != null && remoteTask.modifiedAt >= localModifiedAt) {
                    tasksDao.deleteTask(remoteTask.id)
                }
            }

            localTask == null || remoteTask.modifiedAt > localModifiedAt -> {
                val (taskEntity, subDataEntities) = remoteTask.toEntities()
                tasksDao.applyRemoteTask(taskEntity, subDataEntities)
            }
        }
    }

    private suspend fun applyRemoteRoutine(remoteRoutine: RemoteRoutine) {
        val localRoutine = routinesDao.getRoutineById(remoteRoutine.id)
        val localModifiedAt = localRoutine?.modifiedAt ?: 0L
        when {
            remoteRoutine.deleted -> {
                if (localRoutine != null && remoteRoutine.modifiedAt >= localModifiedAt) {
                    routinesDao.deleteRoutine(remoteRoutine.id)
                }
            }

            localRoutine == null || remoteRoutine.modifiedAt > localModifiedAt -> {
                val (routineEntity, routineItemEntities) = remoteRoutine.toEntities()
                routinesDao.updateRoutineWithItems(routineEntity, routineItemEntities)
            }
        }
    }

    private fun isAfterCursor(updatedAt: String?, id: String, cursor: SyncCursor?): Boolean {
        if (cursor == null) return true
        val timestamp = updatedAt ?: return false
        return timestamp > cursor.updatedAt ||
            (timestamp == cursor.updatedAt && (cursor.lastEntityId == null || id > cursor.lastEntityId))
    }

    private companion object {
        const val PAGE_SIZE = 200
        const val TYPE_TASK = "task"
        const val TYPE_ROUTINE = "routine"
    }
}
