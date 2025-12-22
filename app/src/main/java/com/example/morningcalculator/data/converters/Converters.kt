package com.example.morningcalculator.data.converters

import androidx.room.TypeConverter
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.SubData
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlin.time.Duration

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    // --- SubData List ---
    @TypeConverter
    fun fromSubDataList(value: List<SubData>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toSubDataList(value: String): List<SubData> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- RoutineLink List ---
    @TypeConverter
    fun fromRoutineLinkList(value: List<RoutineLink>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toRoutineLinkList(value: String): List<RoutineLink> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Duration (сохраняем как ISO строку или миллисекунды) ---
    @TypeConverter
    fun fromDuration(value: Duration): String {
        return value.toString()
    }

    @TypeConverter
    fun toDuration(value: String): Duration {
        return Duration.parse(value)
    }

    // --- LocalTime ---
    @TypeConverter
    fun fromLocalTime(value: LocalTime): String {
        return value.toString()
    }

    @TypeConverter
    fun toLocalTime(value: String): LocalTime {
        return LocalTime.parse(value)
    }
}