package com.example.morningcalculator.data.db

import androidx.room.TypeConverter
import kotlinx.datetime.LocalTime
import kotlin.time.Duration

class Converters {
    @TypeConverter
    fun fromDuration(value: Duration): String {
        return value.toString()
    }

    @TypeConverter
    fun toDuration(value: String): Duration {
        return Duration.parse(value)
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime): String {
        return value.toString()
    }

    @TypeConverter
    fun toLocalTime(value: String): LocalTime {
        return LocalTime.parse(value)
    }
}