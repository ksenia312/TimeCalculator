package com.example.morningcalculator.shared.navigator

import kotlinx.serialization.Serializable

@Serializable
data class EditTaskArguments(
    val taskId: String,
    val source: EditTaskSource = EditTaskSource.Standalone,
)

@Serializable
sealed interface EditTaskSource {
    @Serializable
    data object Standalone : EditTaskSource

    @Serializable
    data class Routine(
        val routineId: String,
        val linkId: String,
        val selectedSubDataId: String? = null,
    ) : EditTaskSource
}
