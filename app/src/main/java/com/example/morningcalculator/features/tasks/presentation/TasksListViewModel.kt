package com.example.morningcalculator.features.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TasksListViewModel(
    val repository: TasksRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<TasksListViewState>(TasksListViewState.Loading)
    val viewState: StateFlow<TasksListViewState> = _viewState

    init {
        loadTasks()
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun clearTasks() {
        viewModelScope.launch {
            repository.clearTasks()
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.tasksFlow.collect {
                _viewState.value = TasksListViewState.Success(
                    tasks = it
                )
            }
        }
    }
}


sealed interface TasksListViewState {
    object Loading : TasksListViewState
    data class Success(
        val tasks: List<Task>,
    ) : TasksListViewState

    data class Error(val error: String) : TasksListViewState
}