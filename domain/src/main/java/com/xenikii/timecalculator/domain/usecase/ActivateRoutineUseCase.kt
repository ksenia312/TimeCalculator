package com.xenikii.timecalculator.domain.usecase

import com.xenikii.timecalculator.domain.model.PremiumEntitlementState
import com.xenikii.timecalculator.domain.model.RoutinePauseState
import com.xenikii.timecalculator.domain.repository.FREE_ROUTINE_LIMIT
import com.xenikii.timecalculator.domain.repository.PremiumRepository
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The single place that enforces the free-tier "[FREE_ROUTINE_LIMIT] routines active at once"
 * limit - the only check performed when waking a paused routine, regardless of whether it was
 * [RoutinePauseState.PAUSED_MANUAL] or [RoutinePauseState.PAUSED_AUTO] before: there is one
 * activation path, not two. This is a distinct check from the total-routine-count gate applied at
 * creation time (see the routine editor) - pausing/creating are unrelated to this limit.
 *
 * Pure orchestration over [RoutineRepository] + [PremiumRepository] with no Android/Room
 * dependency, which is why it lives in `domain` rather than following the `data`-module
 * convention used for [com.xenikii.timecalculator.data.schedule.ReconcileRoutinePauseForPremiumUseCase]
 * - both `feature/routineslist` and `feature/routineeditor` need to inject this directly, and
 * neither depends on `data`.
 *
 * Guarded by a [Mutex] so two near-simultaneous activations (e.g. quick taps on two different
 * paused routines from two screens) can't both read the same active count and both slip past the
 * limit - the count is re-read fresh at the moment each call actually runs, never a snapshot the
 * UI captured earlier.
 */
class ActivateRoutineUseCase(
    private val routineRepository: RoutineRepository,
    private val premiumRepository: PremiumRepository,
) {
    private val mutex = Mutex()

    suspend operator fun invoke(routineId: String): Result = mutex.withLock {
        android.util.Log.d("LIMIT_DEBUG", "ActivateRoutineUseCase($routineId): acquired lock @ ${System.currentTimeMillis()}")
        // .first() alone would always grab onStart's UNKNOWN placeholder - wait for a real answer.
        val entitlement = withTimeoutOrNull(ENTITLEMENT_RESOLUTION_TIMEOUT_MILLIS) {
            premiumRepository.observeEntitlementState().first { it != PremiumEntitlementState.UNKNOWN }
        }
        android.util.Log.d("LIMIT_DEBUG", "ActivateRoutineUseCase($routineId): entitlement=$entitlement @ ${System.currentTimeMillis()}")

        if (entitlement == PremiumEntitlementState.ACTIVE) {
            routineRepository.setPauseState(routineId, RoutinePauseState.ACTIVE)
            android.util.Log.d("LIMIT_DEBUG", "ActivateRoutineUseCase($routineId): ACTIVE -> ACTIVATED unconditionally @ ${System.currentTimeMillis()}")
            return@withLock Result.ACTIVATED
        }

        // EXPIRED or still unresolved - both enforce the limit rather than silently letting it slide.
        val routines = routineRepository.routinesFlow.first()
        val activeCount = routines.count { it.state == RoutinePauseState.ACTIVE }
        android.util.Log.d("LIMIT_DEBUG", "ActivateRoutineUseCase($routineId): activeCount=$activeCount (limit=$FREE_ROUTINE_LIMIT), active=${routines.filter { it.state == RoutinePauseState.ACTIVE }.map { it.id }} @ ${System.currentTimeMillis()}")
        if (activeCount >= FREE_ROUTINE_LIMIT) {
            android.util.Log.d("LIMIT_DEBUG", "ActivateRoutineUseCase($routineId): BLOCKED_BY_LIMIT, no write @ ${System.currentTimeMillis()}")
            Result.BLOCKED_BY_LIMIT
        } else {
            android.util.Log.d("LIMIT_DEBUG", "ActivateRoutineUseCase($routineId): under limit, calling setPauseState(ACTIVE) @ ${System.currentTimeMillis()}")
            routineRepository.setPauseState(routineId, RoutinePauseState.ACTIVE)
            android.util.Log.d("LIMIT_DEBUG", "ActivateRoutineUseCase($routineId): setPauseState(ACTIVE) done -> ACTIVATED @ ${System.currentTimeMillis()}")
            Result.ACTIVATED
        }
    }

    enum class Result {
        ACTIVATED,
        BLOCKED_BY_LIMIT,
    }

    private companion object {
        const val ENTITLEMENT_RESOLUTION_TIMEOUT_MILLIS = 4_000L
    }
}
