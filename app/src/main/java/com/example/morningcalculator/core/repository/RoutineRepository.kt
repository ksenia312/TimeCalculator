package com.example.morningcalculator.core.repository

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import kotlinx.coroutines.flow.StateFlow

interface RoutineRepository {
    fun initializeId(id: String)

    fun clearId()

    fun routineFlow(): StateFlow<Routine?>

    fun routinesFlow(): StateFlow<List<Routine>>

    fun addRoutine(request: RoutineRequest)

    fun updateRoutine(routine: Routine)
}