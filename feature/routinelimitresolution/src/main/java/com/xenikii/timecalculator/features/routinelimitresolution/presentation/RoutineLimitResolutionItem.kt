package com.xenikii.timecalculator.features.routinelimitresolution.presentation

data class RoutineLimitResolutionItem(
    val id: String,
    val title: String,
    val lastTriggeredAt: Long?,
    val isSelected: Boolean,
)
