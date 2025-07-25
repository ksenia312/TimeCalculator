package com.example.morningcalculator.core.model

import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    @Serializable(with = LocalTimeIsoSerializer::class) val time: LocalTime,
    val entries: List<RoutineEntry>
)

@OptIn(ExperimentalTime::class)
data class RoutineRequest(
    val title: String,
    val time: LocalTime
)

@OptIn(ExperimentalTime::class)
data class RoutineCombined(
    val routineId: String,
    val title: String,
    val taskPairs: List<Pair<Task, SubData>>,
    val time: LocalTime
)

object LocalTimeIsoSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalTimeIso", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalTime =
        LocalTime.parse(decoder.decodeString())
}