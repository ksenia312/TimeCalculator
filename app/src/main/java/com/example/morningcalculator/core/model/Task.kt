package com.example.morningcalculator.core.model

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val data: List<SubData>
) {
    val dataSortedByDuration = data.sortedBy { it.duration }
}

@Serializable
data class SubData(val id: String = UUID.randomUUID().toString(), val duration: Duration) {
    companion object {
        val tenMins = SubData(duration = 10.minutes)
    }
}