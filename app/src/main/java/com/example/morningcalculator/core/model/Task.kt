package com.example.morningcalculator.core.model

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Duration

@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val data: List<SubData>
)

@Serializable
data class SubData(val id: String = UUID.randomUUID().toString(), val duration: Duration)