package com.xenikii.timecalculator.features.routinelimitresolution.presentation

sealed interface RoutineLimitResolutionViewState {
    data object Loading : RoutineLimitResolutionViewState

    data class Content(
        val items: List<RoutineLimitResolutionItem>,
        val selectedCount: Int,
        val canSave: Boolean,
        val saveState: SaveState,
    ) : RoutineLimitResolutionViewState
}

data class SaveState(
    val inProgress: Boolean = false,
    val failed: Boolean = false,
)
