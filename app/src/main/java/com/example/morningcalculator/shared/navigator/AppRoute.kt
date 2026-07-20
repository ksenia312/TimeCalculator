package com.example.morningcalculator.shared.navigator

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data class Routine(val routineId: String) : AppRoute

    @Serializable
    data class CreateTask(val routineId: String? = null) : AppRoute

    @Serializable
    data class EditTask(val arguments: EditTaskArguments) : AppRoute

    @Serializable
    data object CreateRoutine : AppRoute

    @Serializable
    data class EditRoutine(
        val routineId: String,
        val fromRoutineScreen: Boolean = false,
    ) : AppRoute
}
