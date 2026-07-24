package com.xenikii.timecalculator.domain.model

import kotlin.time.Duration

data class TaskRequest(
    val title: String,
    val description: String,
    val durations: List<Duration>
)

data class TaskUpdateRequest(
    val taskId: String,
    val title: String,
    val description: String,
    val durations: List<Duration>,
)