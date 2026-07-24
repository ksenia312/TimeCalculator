package com.xenikii.timecalculator.data.model

import androidx.room.Entity

@Entity(tableName = "pending_deletions", primaryKeys = ["entityType", "id"])
data class PendingDeletionEntity(
    val entityType: String,
    val id: String,
    val modifiedAt: Long,
)
