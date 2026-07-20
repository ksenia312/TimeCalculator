package com.example.morningcalculator.features.landing.presentation

import com.example.morningcalculator.shared.viewitem.RoutineCardViewItem

data class LandingRoutineState(
    val routineId: String,
    val cardViewItem: RoutineCardViewItem,
    val currentTaskViewItem: LandingCardTaskViewItem?,
    val nextTaskViewItem: LandingCardTaskViewItem?,
)
