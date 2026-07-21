package com.example.morningcalculator.shared.navigator

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    /**
     * Whether this destination shows a bright/accent top bar that reaches under the status bar.
     * When true the status bar icons are forced to light (white); otherwise the app default is
     * kept, so a new screen with an ordinary bar needs no extra wiring.
     */
    val hasBrightTopBar: Boolean get() = false

    @Serializable
    data object Home : AppRoute

    @Serializable
    data class Routine(val routineId: String) : AppRoute, DeepLinkKey {
        override val hasBrightTopBar: Boolean get() = true
        override val parent: NavKey get() = Home
    }

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
