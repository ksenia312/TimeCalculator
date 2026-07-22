package com.example.morningcalculator.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningcalculator.features.home.ui.components.HomeTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val logoutUseCase: suspend () -> Result<Unit>,
) : ViewModel() {

    private val _viewState = MutableStateFlow(HomeViewState())
    val uiState: StateFlow<HomeViewState> = _viewState.asStateFlow()

    fun onTabSelected(tab: HomeTab) {
        _viewState.update { it.copy(selectedTab = tab) }
    }

    fun logout() {
        if (_viewState.value.isLoggingOut) return
        viewModelScope.launch {
            _viewState.update { it.copy(isLoggingOut = true) }
            logoutUseCase()
            _viewState.update { it.copy(isLoggingOut = false) }
        }
    }
}

data class HomeViewState(
    val selectedTab: HomeTab = HomeTab.LANDING,
    val isLoggingOut: Boolean = false,
)
