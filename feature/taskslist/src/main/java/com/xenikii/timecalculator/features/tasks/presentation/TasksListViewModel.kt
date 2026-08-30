package com.xenikii.timecalculator.features.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.domain.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksListViewModel(
    val repository: TasksRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow<TasksListViewState>(TasksListViewState.Loading)
    val viewState: StateFlow<TasksListViewState> = _viewState

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds

    init {
        loadTasks()
    }

    fun toggleSelection(id: String) {
        _selectedIds.update { current ->
            if (id in current) current - id else current + id
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                repository.deleteTask(id)
            }
            _selectedIds.value = emptySet()
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.tasksFlow.collect {
                _viewState.value = TasksListViewState.Success(
                    tasks = it,
                    sorted = it.sortedBy { task -> task.modifiedAt }.reversed()
                )
            }
        }
    }
}


sealed interface TasksListViewState {
    object Loading : TasksListViewState
    data class Success(
        val tasks: List<Task>,
        val sorted: List<Task> = tasks,
    ) : TasksListViewState

    data class Error(val error: String) : TasksListViewState
}