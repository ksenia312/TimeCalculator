package com.xenikii.timecalculator.data.repository

import androidx.room.withTransaction
import com.xenikii.timecalculator.data.db.AppDatabase
import com.xenikii.timecalculator.data.db.SyncDao
import com.xenikii.timecalculator.data.db.TasksDao
import com.xenikii.timecalculator.data.model.PendingDeletionEntity
import com.xenikii.timecalculator.data.model.SubDataEntity
import com.xenikii.timecalculator.data.model.TaskEntity
import com.xenikii.timecalculator.data.sync.SyncTrigger
import com.xenikii.timecalculator.domain.model.SubData
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.domain.model.TaskRequest
import com.xenikii.timecalculator.domain.model.TaskUpdateRequest
import com.xenikii.timecalculator.domain.repository.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import java.util.UUID

class TasksRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val tasksDao: TasksDao,
    private val syncDao: SyncDao,
    private val syncTrigger: SyncTrigger,
) : TasksRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val tasksFlow: Flow<List<Task>> = tasksDao.getTasks()
        .map { list ->
            list.map { populated ->
                Task(
                    id = populated.task.id,
                    title = populated.task.title,
                    description = populated.task.description,
                    modifiedAt = populated.task.modifiedAt,
                    data = populated.subDataList.map { SubData(it.id, it.duration) }
                )
            }
        }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    override fun getTaskFlow(id: String): Flow<Task?> =
        tasksFlow.map { tasks -> tasks.firstOrNull { it.id == id } }

    override suspend fun updateTask(request: TaskUpdateRequest): Task {
        val taskEntity = TaskEntity(
            id = request.taskId,
            title = request.title,
            description = request.description,
            modifiedAt = System.currentTimeMillis()
        )

        val subDataEntities = withContext(Dispatchers.IO) {
            val existingIdsByDuration = tasksDao.getSubDataForTask(request.taskId)
                .groupBy { it.duration }
                .mapValues { (_, entities) -> ArrayDeque(entities.map { it.id }) }

            val reconciled = request.durations.map { duration ->
                val reusedId = existingIdsByDuration[duration]?.removeFirstOrNull()
                SubDataEntity(
                    id = reusedId ?: UUID.randomUUID().toString(),
                    taskId = request.taskId,
                    duration = duration
                )
            }

            tasksDao.updateTaskWithData(taskEntity, reconciled)
            syncTrigger.emit()
            reconciled
        }

        return Task(
            id = taskEntity.id,
            title = taskEntity.title,
            description = taskEntity.description,
            data = subDataEntities.map { SubData(it.id, it.duration) },
            modifiedAt = taskEntity.modifiedAt
        )
    }

    override suspend fun addTask(request: TaskRequest): Task {
        val newTaskId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val taskEntity = TaskEntity(
            id = newTaskId,
            title = request.title,
            description = request.description,
            modifiedAt = timestamp
        )

        val subDataEntities = request.durations.map { duration ->
            SubDataEntity(
                id = UUID.randomUUID().toString(),
                taskId = newTaskId,
                duration = duration
            )
        }

        withContext(Dispatchers.IO) {
            tasksDao.insertTaskWithData(taskEntity, subDataEntities)
            syncTrigger.emit()
        }

        return Task(
            id = taskEntity.id,
            title = taskEntity.title,
            description = taskEntity.description,
            data = subDataEntities.map { SubData(it.id, it.duration) },
            modifiedAt = taskEntity.modifiedAt
        )
    }

    override suspend fun deleteTask(id: String) {
        withContext(Dispatchers.IO) {
            appDatabase.withTransaction {
                syncDao.addPendingDeletion(
                    PendingDeletionEntity(
                        entityType = TYPE_TASK,
                        id = id,
                        modifiedAt = System.currentTimeMillis(),
                    )
                )
                tasksDao.deleteTask(id)
            }
            syncTrigger.emit()
        }
    }

    override suspend fun clearTasks() {
        withContext(Dispatchers.IO) {
            tasksDao.clearTasks()
        }
    }

    private companion object {
        const val TYPE_TASK = "task"
    }
}