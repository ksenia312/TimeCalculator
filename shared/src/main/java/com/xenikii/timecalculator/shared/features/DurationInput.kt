package com.xenikii.timecalculator.shared.features

data class DurationInput(
    val hours: String = "",
    val minutes: String = "",
) {
    fun hasAnyValue(): Boolean = hours.isNotBlank() || minutes.isNotBlank()

    fun totalMinutesOrNull(): Long? {
        if (!hasAnyValue()) return null

        val hoursValue = hours.takeIf(String::isNotBlank)?.toLongOrNull() ?: 0L
        val minutesValue = minutes.takeIf(String::isNotBlank)?.toLongOrNull() ?: 0L

        return (hoursValue * MINUTES_IN_HOUR) + minutesValue
    }

    companion object {
        private const val MINUTES_IN_HOUR = 60L

        fun fromTotalMinutes(totalMinutes: Long): DurationInput {
            val hours = totalMinutes / MINUTES_IN_HOUR
            val minutes = totalMinutes % MINUTES_IN_HOUR

            return DurationInput(
                hours = hours.takeIf { it > 0 }?.toString().orEmpty(),
                minutes = minutes.toString(),
            )
        }
    }
}
