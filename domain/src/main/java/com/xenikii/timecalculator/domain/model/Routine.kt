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
data class Routine(
    val id: String,
    val title: String,
    @Serializable(with = InstantIsoSerializer::class)
    val scheduledAt: Instant,
    val scheduledAtAnchor: RoutineScheduleAnchor = RoutineScheduleAnchor.END,
    val modifiedAt: Long,
    val color: String,
    val data: List<RoutineLink>,
)

@Serializable
data class RoutineRequest(
    val title: String,
    @Serializable(with = InstantIsoSerializer::class)
    val scheduledAt: Instant,
    val scheduledAtAnchor: RoutineScheduleAnchor = RoutineScheduleAnchor.END,
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