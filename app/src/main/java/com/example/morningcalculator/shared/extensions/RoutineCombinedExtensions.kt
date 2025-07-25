package com.example.morningcalculator.shared.extensions

import com.example.morningcalculator.core.model.RoutineCombined
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalTime
import kotlin.time.Duration
import kotlin.time.toJavaDuration

fun RoutineCombined.whenToGetUp(): LocalTime {
    val total: Duration = taskPairs.fold(Duration.ZERO) { acc, (_, task) ->
        acc + task.duration
    }

    return time.toJavaLocalTime().minus(total.toJavaDuration()).toKotlinLocalTime()
}

fun RoutineCombined.timeOnMoment(index: Int): LocalTime {
    val total: Duration = taskPairs.foldIndexed(Duration.ZERO) { currentIndex, acc, (_, task) ->
        if (currentIndex <= index) return@foldIndexed acc
        acc + task.duration
    }

    return time.toJavaLocalTime().minus(total.toJavaDuration()).toKotlinLocalTime()
}