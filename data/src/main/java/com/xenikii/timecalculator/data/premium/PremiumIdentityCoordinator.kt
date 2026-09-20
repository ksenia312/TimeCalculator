package com.xenikii.timecalculator.data.premium

import com.xenikii.timecalculator.domain.repository.AuthRepository
import com.xenikii.timecalculator.domain.repository.PremiumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Keeps the RevenueCat identity aligned with the signed-in Supabase user, so entitlements follow
 * the account across devices/logins. Started once from Application.onCreate, mirroring
 * [com.xenikii.timecalculator.data.sync.SyncManager].
 *
 * EVERY_TASK-vs-premium enforcement doesn't live here: RoutineScheduleRepositoryImpl pins each
 * routine's notification mode when it starts and keeps it for that routine's whole run, so a
 * premium change mid-routine never needs a reactive downgrade - the next routine to start just
 * picks up the current entitlement on its own.
 */
class PremiumIdentityCoordinator(
    private val authRepository: AuthRepository,
    private val premiumRepository: PremiumRepository,
    private val scope: CoroutineScope,
) {
    private var hasStarted = false

    fun start() {
        if (hasStarted) return
        hasStarted = true

        scope.launch {
            authRepository.observeCurrentUser()
                .distinctUntilChanged { previous, current -> previous?.id == current?.id }
                .collect { user ->
                    android.util.Log.d("LOGOUT_DEBUG", "PremiumIdentityCoordinator: user=${user?.id} @ ${System.currentTimeMillis()}")
                    if (user != null) {
                        premiumRepository.identify(user.id)
                    } else {
                        premiumRepository.resetIdentity()
                    }
                    android.util.Log.d("LOGOUT_DEBUG", "PremiumIdentityCoordinator: identify/resetIdentity done for user=${user?.id} @ ${System.currentTimeMillis()}")
                }
        }
    }
}
