package com.xenikii.timecalculator.features.landing.presentation

import com.xenikii.timecalculator.shared.viewitem.RoutineCardViewItem

data class LandingRoutineState(
    val routineId: String,
    val cardViewItem: RoutineCardViewItem,
    val completedTasks: List<LandingCardTaskViewItem>,
    val previewTasks: List<LandingCardTaskViewItem>,
    val futureTasks: List<LandingCardTaskViewItem>,
    val hasHiddenTasks: Boolean,
)
