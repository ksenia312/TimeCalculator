package com.xenikii.timecalculator.domain.repository

import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.domain.model.TaskRequest
import com.xenikii.timecalculator.domain.model.TaskUpdateRequest
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    val tasksFlow: Flow<List<Task>>
    fun getTaskFlow(id: String): Flow<Task?>

    suspend fun addTask(request: TaskRequest): Task

    suspend fun updateTask(request: TaskUpdateRequest): Task

    suspend fun deleteTask(id: String)

    suspend fun clearTasks()
}