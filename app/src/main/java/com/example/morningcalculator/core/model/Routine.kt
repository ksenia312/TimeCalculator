package com.example.morningcalculator.core.model

import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class Routine(
    val id: String,
    val title: String,
    @Serializable(with = LocalTimeIsoSerializer::class) val time: LocalTime,
    val modifiedAt: Long,
    val color: String,
    val data: List<RoutineLink>,
)

@OptIn(ExperimentalTime::class)
data class RoutineRequest(
    val title: String,
    val time: LocalTime,
    val color: String
)

object LocalTimeIsoSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalTimeIso", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString())
}