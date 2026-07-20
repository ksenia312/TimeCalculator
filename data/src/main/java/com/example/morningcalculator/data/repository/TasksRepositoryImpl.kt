package com.example.morningcalculator.data.repository

import com.example.morningcalculator.domain.model.SubData
import com.example.morningcalculator.domain.model.Task
import com.example.morningcalculator.domain.model.TaskRequest
import com.example.morningcalculator.domain.model.TaskUpdateRequest
import com.example.morningcalculator.domain.repository.TasksRepository
import com.example.morningcalculator.data.db.TasksDao
import com.example.morningcalculator.data.model.SubDataEntity
import com.example.morningcalculator.data.model.TaskEntity
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
    private val dao: TasksDao
) : TasksRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val tasksFlow: Flow<List<Task>> = dao.getTasks()
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
            val existingIdsByDuration = dao.getSubDataForTask(request.taskId)
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

            dao.updateTaskWithData(taskEntity, reconciled)
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
            dao.insertTaskWithData(taskEntity, subDataEntities)
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
            dao.deleteTask(id)
        }
    }

    override suspend fun clearTasks() {
        withContext(Dispatchers.IO) {
            dao.clearTasks()
        }
    }
}