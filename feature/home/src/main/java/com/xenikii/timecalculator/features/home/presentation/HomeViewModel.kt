package com.xenikii.timecalculator.features.home.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xenikii.timecalculator.domain.repository.SyncStateProvider
import com.xenikii.timecalculator.features.home.ui.components.HomeTab
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle,
    syncStateProvider: SyncStateProvider,
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        HomeViewState(selectedTab = savedStateHandle.restoredSelectedTab())
    )
    val uiState: StateFlow<HomeViewState> = _viewState.asStateFlow()
    val isSyncing: StateFlow<Boolean> = syncStateProvider.isSyncing.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = syncStateProvider.isSyncing.value,
    )

    fun onTabSelected(tab: HomeTab) {
        _viewState.update { it.copy(selectedTab = tab) }
        savedStateHandle[SELECTED_TAB_KEY] = tab.name
    }

    private fun SavedStateHandle.restoredSelectedTab(): HomeTab =
        get<String>(SELECTED_TAB_KEY)?.let { name ->
            HomeTab.entries.find { it.name == name }
        } ?: HomeTab.LANDING

    private companion object {
        const val SELECTED_TAB_KEY = "selected_tab"
    }
}

data class HomeViewState(
    val selectedTab: HomeTab = HomeTab.LANDING,
)
