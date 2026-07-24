package com.xenikii.timecalculator.data.db

import androidx.room.TypeConverter
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
}