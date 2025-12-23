package com.example.morningcalculator.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalTime
import java.util.UUID


@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val color: String,
    val time: LocalTime,
    val modifiedAt: Long
)