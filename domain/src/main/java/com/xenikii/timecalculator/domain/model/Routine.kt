package com.xenikii.timecalculator.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

@Serializable
enum class RoutineScheduleAnchor {
    START,
    END,
}

@Serializable
enum class RoutineRecurrenceUnit {
    NONE,
    DAY,
    WEEK,
    MONTH,
    YEAR,
}

@Serializable
data class RoutineRecurrence(
    val interval: Int = 1,
    val unit: RoutineRecurrenceUnit = RoutineRecurrenceUnit.NONE,
    /**
     * ISO day-of-week numbers (1 = Monday .. 7 = Sunday) the routine repeats on.
     * Only meaningful when [unit] is [RoutineRecurrenceUnit.WEEK]. An empty set means the
     * routine repeats on the same weekday as its scheduled date.
     */
    val daysOfWeek: Set<Int> = emptySet(),
)

/**
 * Whether a routine is scheduled at all. This is state, not a limits system: pausing never
 * changes the free-tier routine count check at creation time.
 */
@Serializable
enum class RoutinePauseState {
    /** Scheduled normally. */
    ACTIVE,

    /** User paused it themselves (e.g. a vacation) - never touched automatically. */
    PAUSED_MANUAL,

    /** Auto-paused because premium expired with more than the free limit active - resumed
     * automatically the moment premium becomes active again. */
    PAUSED_AUTO,
}

@Serializable
data class Routine(
    val id: String,
    val title: String,
    @Serializable(with = InstantIsoSerializer::class)
    val scheduledAt: Instant,
    val scheduledAtAnchor: RoutineScheduleAnchor = RoutineScheduleAnchor.END,
    val recurrence: RoutineRecurrence = RoutineRecurrence(),
    val modifiedAt: Long,
    val color: String,
    val data: List<RoutineLink>,
    val state: RoutinePauseState = RoutinePauseState.ACTIVE,
    /** Epoch millis of the last time this routine's alarm actually fired; null if never. */
    val lastTriggeredAt: Long? = null,
) {
    val isActive: Boolean get() = state == RoutinePauseState.ACTIVE
    val isPaused: Boolean get() = !isActive
}

@Serializable
data class RoutineRequest(
    val title: String,
    @Serializable(with = InstantIsoSerializer::class)
    val scheduledAt: Instant,
    val scheduledAtAnchor: RoutineScheduleAnchor = RoutineScheduleAnchor.END,
    val recurrence: RoutineRecurrence = RoutineRecurrence(),
    val color: String,
)

object InstantIsoSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InstantIso", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}