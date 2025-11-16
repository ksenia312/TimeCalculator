package com.example.morningcalculator.core.repository

import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.model.TaskUpdateRequest
import kotlinx.coroutines.flow.StateFlow

interface TasksRepository {
    fun addTask(request: TaskRequest): Task

    fun tasksFlow(): StateFlow<List<Task>>

    fun updateTask(request: TaskUpdateRequest) : Task

    fun clearTasks()

    fun deleteTask(id: String)
}

class PreviewTasksRepository : TasksRepository {
    override fun addTask(request: TaskRequest): Task {
        throw NotImplementedError("PreviewTasksRepository does not implement addTask")
    }

    override fun tasksFlow(): StateFlow<List<Task>> {
        throw NotImplementedError("PreviewTasksRepository does not implement tasksFlow")
    }

    override fun updateTask(request: TaskUpdateRequest): Task {
        throw NotImplementedError("PreviewTasksRepository does not implement updateTask")
    }

    override fun clearTasks() {
        throw NotImplementedError("PreviewTasksRepository does not implement clearTasks")
    }

    override fun deleteTask(id: String) {
        throw NotImplementedError("PreviewTasksRepository does not implement deleteTask")
    }
}