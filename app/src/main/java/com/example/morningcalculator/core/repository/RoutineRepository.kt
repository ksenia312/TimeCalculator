package com.example.morningcalculator.core.repository

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import kotlinx.coroutines.flow.StateFlow

interface RoutineRepository {
    fun initializeId(id: String)

    fun clearId()

    val routineFlow: StateFlow<Routine.Links?>

    val routinesFlow: StateFlow<List<Routine.Links>>

    fun addRoutine(request: RoutineRequest)

    fun updateRoutine(routine: Routine.Links)
}
