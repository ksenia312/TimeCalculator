package com.example.morningcalculator.core.model

import kotlinx.serialization.Serializable

@Serializable
data class RoutineLink(
    val taskId: String,
    val subDataId: String
)