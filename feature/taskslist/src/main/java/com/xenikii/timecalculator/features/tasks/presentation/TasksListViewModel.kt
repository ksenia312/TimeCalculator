package com.xenikii.timecalculator.features.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.model.Task
import com.xenikii.timecalculator.domain.model.TaskUpdateRequest
import com.xenikii.timecalculator.domain.repository.TasksRepository
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