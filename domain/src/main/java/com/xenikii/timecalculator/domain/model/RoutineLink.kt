package com.xenikii.timecalculator.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RoutineLink(
    val id: String,
    val task: Task,
    val subData: SubData?
)