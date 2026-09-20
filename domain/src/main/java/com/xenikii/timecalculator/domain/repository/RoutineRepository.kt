package com.xenikii.timecalculator.domain.repository

import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutinePauseState
import com.xenikii.timecalculator.domain.model.RoutineRequest
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface RoutineRepository {
    val routinesFlow: Flow<List<Routine>>

    suspend fun getRoutines(): List<Routine>

    suspend fun getRoutineCount(): Int

    fun getRoutineFlow(id: String): Flow<Routine?>

    suspend fun addRoutine(request: RoutineRequest): String

    suspend fun updateRoutine(routine: Routine)

    suspend fun deleteRoutine(id: String)

    /** No-op if `routineId` is already in `state`. */
    suspend fun setPauseState(routineId: String, state: RoutinePauseState)

    /** Same as [setPauseState] for multiple routines, in one transaction - so observers of
     * [routinesFlow] never see an intermediate state mid-batch. */
    suspend fun setPauseStates(changes: Map<String, RoutinePauseState>)

    /**
     * Records that `routineId`'s alarm actually fired at [triggeredAt]. This is bookkeeping, not
     * a user edit: it must never be treated the same as [updateRoutine] for conflict-resolution
     * purposes (see `lastTriggeredAt` field doc on [Routine]).
     */
    suspend fun recordRoutineTriggered(routineId: String, triggeredAt: Instant)
}