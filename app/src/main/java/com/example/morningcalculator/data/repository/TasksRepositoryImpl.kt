package com.example.morningcalculator.data.repository

import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.model.TaskUpdateRequest
import com.example.morningcalculator.core.repository.TasksRepository
import com.example.morningcalculator.data.db.TasksDao
import com.example.morningcalculator.data.model.SubDataEntity
import com.example.morningcalculator.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.map

class TasksRepositoryImpl(
    private val dao: TasksDao
) : TasksRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val tasksFlow: StateFlow<List<Task>> = dao.getTasks()
        .map { list ->
            list.map { populated ->
                Task(
                    id = populated.task.id,
                    title = populated.task.title,
                    description = populated.task.description,
                    modifiedAt = populated.task.modifiedAt,
                    data = populated.subDataList.map {
                        SubData(it.id, it.duration)
                    }
                )
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override fun updateTask(request: TaskUpdateRequest): Task {
        val taskEntity = TaskEntity(
            id = request.taskId,
            title = request.title,
            description = request.description,
            modifiedAt = System.currentTimeMillis()
        )

        val subDataEntities = request.subData.map {
            SubDataEntity(id = it.id, taskId = request.taskId, duration = it.duration)
        }

        scope.launch {
            dao.updateTaskWithData(taskEntity, subDataEntities)
        }

        return Task(
            id = taskEntity.id,
            title = taskEntity.title,
            description = taskEntity.description,
            data = request.subData,
            modifiedAt = taskEntity.modifiedAt
        )
    }

    override fun addTask(request: TaskRequest): Task {
        val newTaskId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val taskEntity = TaskEntity(
            id = newTaskId,
            title = request.title,
            description = request.description,
            modifiedAt = timestamp,
        )

        val subDataEntities = request.durations.map { duration ->
            SubDataEntity(
                id = UUID.randomUUID().toString(),
                taskId = newTaskId,
                duration = duration
            )
        }

        scope.launch {
            dao.insertTask(taskEntity)
            dao.insertSubData(subDataEntities)
        }

        return Task(
            id = taskEntity.id,
            title = taskEntity.title,
            description = taskEntity.description,
            data = subDataEntities.map { SubData(it.id, it.duration) },
            modifiedAt = taskEntity.modifiedAt
        )
    }

    override fun deleteTask(id: String) {
        scope.launch {
            dao.deleteTask(id)
        }
    }

    override fun clearTasks() {
        scope.launch {
            dao.clearTasks()
        }
    }
}