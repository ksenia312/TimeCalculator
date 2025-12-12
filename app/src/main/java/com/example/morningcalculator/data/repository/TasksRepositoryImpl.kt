package com.example.morningcalculator.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.model.TaskRequest
import com.example.morningcalculator.core.model.TaskUpdateRequest
import com.example.morningcalculator.core.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

class TasksRepositoryImpl(
    context: Context, private val prefs: SharedPreferences = context.getSharedPreferences(
        "tasks", Context.MODE_PRIVATE
    )
) : TasksRepository {

    companion object {
        private const val KEY_TASKS = "tasks_json"
    }

    private val _tasksFlow = MutableStateFlow(loadTasksFromPrefs())

    override val tasksFlow: StateFlow<List<Task>> = _tasksFlow.asStateFlow()

    override fun updateTask(request: TaskUpdateRequest): Task {
        val task = _tasksFlow.value.first { it.id == request.taskId }
        val updatedTask =
            task.copy(
                title = request.title,
                description = request.description,
                data = request.subData,
                modifiedAt = System.currentTimeMillis()
            )
        val updated = _tasksFlow.value.map { if (it.id == updatedTask.id) updatedTask else it }
        saveTasksToPrefs(updated)
        return updatedTask
    }

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_TASKS) _tasksFlow.value = loadTasksFromPrefs()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun addTask(request: TaskRequest): Task {
        val newTask = Task(
            title = request.title,
            description = request.description,
            data = request.durations.map { SubData(duration = it) },
            modifiedAt = System.currentTimeMillis()
        )

        val updated = _tasksFlow.value + newTask
        saveTasksToPrefs(updated)
        return newTask
    }

    override fun deleteTask(id: String) {
        val updated = _tasksFlow.value.filterNot { it.id == id }
        saveTasksToPrefs(updated)
    }

    override fun clearTasks() {
        saveTasksToPrefs(emptyList())
    }

    private fun saveTasksToPrefs(tasks: List<Task>) {
        prefs.edit(commit = false) {
            putString(KEY_TASKS, Json.encodeToString(tasks))
        }
        _tasksFlow.value = tasks
    }

    private fun loadTasksFromPrefs(): List<Task> {
        val json = prefs.getString(KEY_TASKS, "[]") ?: "[]"
        return runCatching { Json.decodeFromString<List<Task>>(json) }.getOrDefault(
            emptyList()
        )
    }
}