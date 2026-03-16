package com.example.morningcalculator.core.repository

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import kotlinx.coroutines.flow.StateFlow

interface RoutineRepository {
    val routinesFlow: StateFlow<List<Routine.Full>>
    val routineFlow: StateFlow<Routine.Full?>

    fun initializeId(id: String)
    fun clearId()

    suspend fun addRoutine(request: RoutineRequest)

    suspend fun updateRoutine(routine: Routine.Full)

    suspend fun deleteRoutine(id: String)
}