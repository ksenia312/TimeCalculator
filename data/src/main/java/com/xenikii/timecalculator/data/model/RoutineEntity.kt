package com.xenikii.timecalculator.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val color: String,
    val scheduledAtMillis: Long,
    val scheduledAtAnchor: String,
    val modifiedAt: Long,
    val pendingSync: Boolean = true,
)