package com.example.morningcalculator.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Entity(tableName = "tasks")
@Serializable
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val data: List<SubData>,
    val modifiedAt: Long? = null
) {
    @androidx.room.Ignore
    val dataSortedByDuration = data.sortedBy { it.duration }
}

@Serializable
data class SubData(val id: String = UUID.randomUUID().toString(), val duration: Duration) {
    companion object {
        val tenMins = SubData(duration = 10.minutes)
    }
}