package com.xenikii.timecalculator.data.premium

import com.xenikii.timecalculator.data.schedule.ReconcileRoutinePauseForPremiumUseCase
import com.xenikii.timecalculator.domain.model.PremiumEntitlementState
import com.xenikii.timecalculator.domain.repository.PremiumRepository
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Pauses excess routines as premium expires - the one-way safety net. Reacts to both
 * [RoutineRepository.routinesFlow] and [PremiumRepository.observeEntitlementState], so a premium
 * change is picked up the moment RevenueCat's listener delivers it rather than waiting on the
 * periodic reconcile watchdog. Started once from Application.onCreate, before any
 * boot/watchdog-triggered component can run in the same process, mirroring
 * [PremiumIdentityCoordinator].
 *
 * Only acts on [PremiumEntitlementState.EXPIRED] - never [PremiumEntitlementState.UNKNOWN] (must
 * not act before RevenueCat has actually answered), and never
 * [PremiumEntitlementState.ACTIVE] either: nothing in this app wakes a paused routine
 * automatically (see [RoutinePauseState][com.xenikii.timecalculator.domain.model.RoutinePauseState]'s
 * doc), so premium returning is simply not this coordinator's concern. Writes are diffed against
 * the current routines so a no-op reconciliation (the common case) never touches Room, which also
 * keeps this from looping against its own writes through routinesFlow.
 */
class RoutineAutoPauseCoordinator(
    private val routineRepository: RoutineRepository,
    private val premiumRepository: PremiumRepository,
    private val reconcilePauseState: ReconcileRoutinePauseForPremiumUseCase,
    private val scope: CoroutineScope,
) {
    private var hasStarted = false

    @OptIn(FlowPreview::class)
    fun start() {
        if (hasStarted) return
        hasStarted = true

        scope.launch {
            combine(
                routineRepository.routinesFlow,
                premiumRepository.observeEntitlementState(),
            ) { routines, entitlement -> routines to entitlement }
                .debounce(DEBOUNCE_MILLIS.milliseconds)
                .collect { (routines, entitlement) ->
                    if (entitlement != PremiumEntitlementState.EXPIRED) return@collect

                    val reconciled = reconcilePauseState(routines, entitlement)
                    routines.zip(reconciled).forEach { (before, after) ->
                        if (before.state != after.state) {
                            routineRepository.setPauseState(after.id, after.state)
                        }
                    }
                }
        }
    }

    private companion object {
        const val DEBOUNCE_MILLIS = 300L
    }
}
