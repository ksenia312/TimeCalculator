package com.xenikii.timecalculator.data.schedule

import com.xenikii.timecalculator.domain.model.PremiumEntitlementState
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutinePauseState
import com.xenikii.timecalculator.domain.repository.FREE_ROUTINE_LIMIT

/**
 * Pure policy for reconciling routine pause state against premium entitlement - no Android/Room
 * dependency, so [RoutineAutoPauseCoordinator][com.xenikii.timecalculator.data.premium.RoutineAutoPauseCoordinator]
 * (stage 1) and any later worker/receiver/preselection-screen reusing this same rule apply
 * identical behavior.
 *
 * Which routines stay active is decided by [activePriorityComparator], keyed on
 * [Routine.lastTriggeredAt] - the real "this routine's alarm actually fired" signal, stamped in
 * [com.xenikii.timecalculator.data.schedule.repository.RoutineScheduleRepositoryImpl.handleAlarm].
 *
 * Idempotent: calling this repeatedly with the same inputs returns routines unchanged once the
 * target state is reached, and free users (<= [FREE_ROUTINE_LIMIT] active routines) never have
 * anything to reconcile in the first place.
 */
class ReconcileRoutinePauseForPremiumUseCase {

    operator fun invoke(routines: List<Routine>, entitlement: PremiumEntitlementState): List<Routine> =
        when (entitlement) {
            PremiumEntitlementState.UNKNOWN -> routines

            PremiumEntitlementState.ACTIVE -> routines.map { routine ->
                if (routine.state == RoutinePauseState.PAUSED_AUTO) {
                    routine.copy(state = RoutinePauseState.ACTIVE)
                } else {
                    routine
                }
            }

            PremiumEntitlementState.EXPIRED -> {
                val active = routines.filter { it.state == RoutinePauseState.ACTIVE }
                if (active.size <= FREE_ROUTINE_LIMIT) {
                    routines
                } else {
                    val keepIds = active
                        .sortedWith(activePriorityComparator)
                        .take(FREE_ROUTINE_LIMIT)
                        .mapTo(mutableSetOf()) { it.id }
                    routines.map { routine ->
                        if (routine.state == RoutinePauseState.ACTIVE && routine.id !in keepIds) {
                            routine.copy(state = RoutinePauseState.PAUSED_AUTO)
                        } else {
                            routine
                        }
                    }
                }
            }
        }

    companion object {
        /**
         * Highest priority (most recently triggered, so most eligible to stay active) first.
         * Routines that have never fired ([Routine.lastTriggeredAt] == null) rank lowest, below
         * any routine that has fired even once - modeled by substituting [Long.MIN_VALUE], which
         * never collides with a real epoch-millis value. Ties (including null == null) break
         * deterministically by id, since this codebase has no `createdAt` field to break by
         * instead.
         *
         * Exposed so a later preselection screen (stage 2) reuses this exact ordering instead of
         * duplicating it.
         */
        val activePriorityComparator: Comparator<Routine> =
            compareByDescending<Routine> { it.lastTriggeredAt ?: Long.MIN_VALUE }.thenBy { it.id }
    }
}
