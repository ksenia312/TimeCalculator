package com.xenikii.timecalculator.data.premium

import com.xenikii.timecalculator.data.schedule.RefreshRoutineNotificationsUseCase
import com.xenikii.timecalculator.domain.model.NotificationMode
import com.xenikii.timecalculator.domain.repository.AuthRepository
import com.xenikii.timecalculator.domain.repository.NotificationSettingsRepository
import com.xenikii.timecalculator.domain.repository.PremiumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Keeps the RevenueCat identity aligned with the signed-in Supabase user (so entitlements follow
 * the account across devices/logins), and guards against a premium "lapse": EVERY_TASK is a
 * premium-only notification mode, so if entitlement is lost while it's selected, this falls back
 * to START_AND_END and refreshes scheduled notifications to match. Started once from
 * Application.onCreate, mirroring [com.xenikii.timecalculator.data.sync.SyncManager].
 */
class PremiumIdentityCoordinator(
    private val authRepository: AuthRepository,
    private val premiumRepository: PremiumRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val refreshRoutineNotifications: RefreshRoutineNotificationsUseCase,
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
                    if (user != null) {
                        premiumRepository.identify(user.id)
                    } else {
                        premiumRepository.resetIdentity()
                    }
                }
        }

        scope.launch {
            premiumRepository.observeIsPremium()
                .filter { isPremium ->
                    !isPremium && notificationSettingsRepository.getMode() == NotificationMode.EVERY_TASK
                }
                .collect {
                    notificationSettingsRepository.setMode(NotificationMode.START_AND_END)
                    refreshRoutineNotifications()
                }
        }
    }
}
