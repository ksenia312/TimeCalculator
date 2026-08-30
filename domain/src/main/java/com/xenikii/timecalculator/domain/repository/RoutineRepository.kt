package com.xenikii.timecalculator.domain.repository

import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineRequest
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    val routinesFlow: Flow<List<Routine>>

    suspend fun getRoutines(): List<Routine>

    fun getRoutineFlow(id: String): Flow<Routine?>

    suspend fun addRoutine(request: RoutineRequest)

    suspend fun updateRoutine(routine: Routine)

    suspend fun deleteRoutine(id: String)
}