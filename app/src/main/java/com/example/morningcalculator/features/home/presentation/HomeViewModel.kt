package com.example.morningcalculator.features.home.presentation

import androidx.lifecycle.ViewModel
import com.example.morningcalculator.features.home.ui.components.HomeTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
) : ViewModel() {

    private val _viewState = MutableStateFlow(HomeViewState())
    val uiState: StateFlow<HomeViewState> = _viewState.asStateFlow()

    fun onTabSelected(tab: HomeTab) {
        _viewState.update { it.copy(selectedTab = tab) }
    }
}

data class HomeViewState(
    val selectedTab: HomeTab = HomeTab.LANDING,
)
