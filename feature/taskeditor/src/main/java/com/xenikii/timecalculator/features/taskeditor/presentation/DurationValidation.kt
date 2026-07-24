package com.xenikii.timecalculator.features.taskeditor.presentation

import kotlin.time.Duration

fun List<Duration>.hasDuplicateDurations(): Boolean = size != toSet().size
