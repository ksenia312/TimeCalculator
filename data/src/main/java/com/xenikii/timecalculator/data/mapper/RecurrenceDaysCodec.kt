package com.xenikii.timecalculator.data.mapper

/**
 * Encodes/decodes the set of ISO day-of-week numbers (1 = Monday .. 7 = Sunday) the routine
 * repeats on. Stored locally as a sorted, comma-separated string (empty when no specific days).
 */
fun Set<Int>.encodeRecurrenceDaysOfWeek(): String =
    filter { it in 1..7 }.toSortedSet().joinToString(separator = ",")

fun String.decodeRecurrenceDaysOfWeek(): Set<Int> =
    split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it in 1..7 }
        .toSet()

fun List<Int>.sanitizeRecurrenceDaysOfWeek(): Set<Int> =
    filter { it in 1..7 }.toSortedSet()
