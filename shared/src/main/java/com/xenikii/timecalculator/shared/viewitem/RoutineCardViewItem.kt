package com.xenikii.timecalculator.shared.viewitem

import androidx.annotation.StringRes
import kotlin.time.Duration
import kotlin.time.Instant

data class RoutineCardViewItem(
    val isOngoing: Boolean,
    val isCompleted: Boolean,
    @param:StringRes val startLabelRes: Int,
    @param:StringRes val endLabelRes: Int,
    val startInstant: Instant,
    val endInstant: Instant,
    val title: String,
    val willStartIn: Duration,
)
