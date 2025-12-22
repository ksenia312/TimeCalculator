package com.example.morningcalculator.data.repository

import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.model.TaskUpdateRequest
import com.example.morningcalculator.core.repository.TasksRepository
import com.example.morningcalculator.data.db.TasksDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TasksRepositoryImpl(
    private val dao: TasksDao
) : TasksRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val tasksFlow: StateFlow<List<Task>> = dao.getTasks()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override fun updateTask(request: TaskUpdateRequest): Task {
        val updatedTask = Task(
            id = request.taskId,
            title = request.title,
            description = request.description,
            data = request.subData,
            modifiedAt = System.currentTimeMillis()
        )

        scope.launch {
            dao.insertTask(updatedTask)
        }
        return updatedTask
    }

    override fun addTask(request: TaskRequest): Task {
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = request.title,
            description = request.description,
            data = request.durations.map { SubData(duration = it) },
            modifiedAt = System.currentTimeMillis()
        )

        scope.launch {
            dao.insertTask(newTask)
        }
        return newTask
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