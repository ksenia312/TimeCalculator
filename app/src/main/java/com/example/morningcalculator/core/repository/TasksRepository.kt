package com.example.morningcalculator.core.repository

import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.model.TaskUpdateRequest
import kotlinx.coroutines.flow.StateFlow

interface TasksRepository {
    fun addTask(request: TaskRequest): Task

    val tasksFlow: StateFlow<List<Task>>

    fun updateTask(request: TaskUpdateRequest): Task

    fun clearTasks()

    fun deleteTask(id: String)
}