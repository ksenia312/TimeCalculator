package com.xenikii.timecalculator.features.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.domain.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksListViewModel(
    val repository: TasksRepository,
) : ViewModel() {

    val viewState: StateFlow<TasksListViewState> = repository.tasksFlow
        .map { tasks ->
            runCatching {
                TasksListViewState.Success(
                    tasks = tasks,
                    sorted = tasks.sortedBy { task -> task.modifiedAt }.reversed(),
                )
            }
        }
        .scan<Result<TasksListViewState.Success>, TasksListViewState>(TasksListViewState.Loading) { previous, result ->
            result.getOrElse { error ->
                previous as? TasksListViewState.Success
                    ?: TasksListViewState.Error(error.message.orEmpty())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TasksListViewState.Loading,
        )

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds

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
}


sealed interface TasksListViewState {
    object Loading : TasksListViewState
    data class Success(
        val tasks: List<Task>,
        val sorted: List<Task> = tasks,
    ) : TasksListViewState

    data class Error(val error: String) : TasksListViewState
}