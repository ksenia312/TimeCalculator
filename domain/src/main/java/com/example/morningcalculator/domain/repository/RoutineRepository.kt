package com.example.morningcalculator.domain.repository

import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RoutineRepository {
    val routinesFlow: StateFlow<List<Routine>>

    fun getRoutineFlow(id: String): Flow<Routine?>

    suspend fun addRoutine(request: RoutineRequest)

    suspend fun updateRoutine(routine: Routine)

    suspend fun deleteRoutine(id: String)
}