package com.example.morningcalculator.data.sync.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteTask(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("sub_data") val subData: List<RemoteSubData> = emptyList(),
    @SerialName("modified_at") val modifiedAt: Long,
    val deleted: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class RemoteSubData(
    val id: String,
    val duration: String,
)

@Serializable
data class RemoteRoutine(
    val id: String,
    val title: String,
    val color: String,
    @SerialName("scheduled_at_millis") val scheduledAtMillis: Long,
    @SerialName("scheduled_at_anchor") val scheduledAtAnchor: String,
    val items: List<RemoteRoutineItem> = emptyList(),
    @SerialName("modified_at") val modifiedAt: Long,
    val deleted: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class RemoteRoutineItem(
    val id: String,
    @SerialName("task_id") val taskId: String,
    @SerialName("sub_data_id") val subDataId: String? = null,
    @SerialName("order_index") val orderIndex: Int,
)
