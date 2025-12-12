package com.example.morningcalculator.shared.extensions

import com.example.morningcalculator.core.model.Routine
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalTime
import kotlin.time.Duration
import kotlin.time.toJavaDuration

fun Routine.Full.whenToStart(): LocalTime {
    val total: Duration = data.fold(Duration.ZERO) { acc, link ->
        acc + link.subData.duration
    }

    return time.toJavaLocalTime().minus(total.toJavaDuration()).toKotlinLocalTime()
}

fun Routine.Full.timeOnMoment(index: Int): LocalTime {
    val total: Duration = data.foldIndexed(Duration.ZERO) { currentIndex, acc, link ->
        if (currentIndex <= index) return@foldIndexed acc
        acc + link.subData.duration
    }

    return time.toJavaLocalTime().minus(total.toJavaDuration()).toKotlinLocalTime()
}