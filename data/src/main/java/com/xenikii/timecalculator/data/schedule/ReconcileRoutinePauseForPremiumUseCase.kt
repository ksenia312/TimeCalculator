package com.xenikii.timecalculator.data.schedule

import com.xenikii.timecalculator.domain.model.PremiumEntitlementState
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutinePauseState
import com.xenikii.timecalculator.domain.repository.FREE_ROUTINE_LIMIT

/**
 * Pure policy for the premium-expiry safety net - no Android/Room dependency, so
 * [RoutineAutoPauseCoordinator][com.xenikii.timecalculator.data.premium.RoutineAutoPauseCoordinator]
 * and the routine-limit-resolution screen apply identical rules.
 *
 * This is one-way: it only ever pauses excess routines on confirmed expiry. Nothing here (or
 * anywhere else in the app) wakes a paused routine automatically, regardless of pause reason -
 * see [RoutinePauseState]'s doc for why that was deliberately removed. Waking a routine is a
 * user action (routine list).
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
            PremiumEntitlementState.UNKNOWN, PremiumEntitlementState.ACTIVE -> routines

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

        /**
         * True when premium is confirmed expired and the user has more routines than the free
         * limit that aren't a deliberate manual pause - i.e. there's something for the
         * routine-limit-resolution screen to resolve. This is the ONLY place that counts the
         * "too many routines" conflict; navigation/ViewModel code must call this rather than
         * inspecting pause states itself.
         */
        fun hasUnresolvedLimitConflict(routines: List<Routine>, entitlement: PremiumEntitlementState): Boolean =
            entitlement == PremiumEntitlementState.EXPIRED &&
                routines.count { it.state != RoutinePauseState.PAUSED_MANUAL } > FREE_ROUTINE_LIMIT
    }
}
