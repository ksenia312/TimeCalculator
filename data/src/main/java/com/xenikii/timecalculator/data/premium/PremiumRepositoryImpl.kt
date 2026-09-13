package com.xenikii.timecalculator.data.premium

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.EntitlementInfo
import com.revenuecat.purchases.PeriodType
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.xenikii.timecalculator.domain.model.PremiumPeriodType
import com.xenikii.timecalculator.domain.model.PremiumSource
import com.xenikii.timecalculator.domain.model.PremiumStatus
import com.xenikii.timecalculator.domain.repository.PREMIUM_ENTITLEMENT_ID
import com.xenikii.timecalculator.domain.repository.PremiumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.Instant

/**
 * Wraps [Purchases.sharedInstance] and [GrantedPremiumDataSource]: a user is premium if either
 * source says so, purchase taking priority when both happen to apply. RevenueCat SDK 10.x only
 * exposes coroutine `await*` extensions for offerings/purchase/restore -
 * [Purchases.getCustomerInfo], [Purchases.logIn] and [Purchases.logOut] are still callback-only,
 * so those three are bridged here manually with [suspendCancellableCoroutine].
 */
class PremiumRepositoryImpl(
    private val scope: CoroutineScope,
    private val grantedPremiumDataSource: GrantedPremiumDataSource,
) : PremiumRepository {

    // Manually-granted premium (gifts/founder/promo) is read once per login/session start (see
    // identify()) and refreshed on app foreground (see init below) - it's admin-set and changes
    // rarely, so there's no need to hit Supabase on every isPremiumNow()/observeIsPremium() check.
    private val grantedPremiumFlow = MutableStateFlow(GrantedPremiumInfo.None)

    @Volatile
    private var lastForegroundRefreshAtMillis = 0L

    init {
        // RevenueCat already refreshes CustomerInfo on its own when the app returns to the
        // foreground (PurchasesOrchestrator.onAppForegrounded -> shouldRefreshCustomerInfo), but
        // that only fires once its own cache is considered stale. The Supabase-sourced grant has
        // no such built-in refresh, so this mirrors SyncManager's ProcessLifecycleOwner hook to
        // force both sources current on resume, throttled so a user rapidly switching apps
        // doesn't spam Supabase.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                val now = System.currentTimeMillis()
                if (now - lastForegroundRefreshAtMillis < FOREGROUND_REFRESH_THROTTLE_MILLIS) return
                lastForegroundRefreshAtMillis = now
                Purchases.sharedInstance.getCustomerInfo(CacheFetchPolicy.FETCH_CURRENT, NoopReceiveCustomerInfoCallback)
                scope.launch { refreshGrantedPremium() }
            }
        })
    }

    private val customerInfoFlow: Flow<CustomerInfo> = callbackFlow {
        val listener = UpdatedCustomerInfoListener { info -> trySend(info) }
        Purchases.sharedInstance.updatedCustomerInfoListener = listener
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                trySend(customerInfo)
            }

            override fun onError(error: PurchasesError) {
                // No emission on failure; the listener above still delivers updates once
                // customer info becomes available (e.g. once connectivity returns).
            }
        })
        awaitClose { Purchases.sharedInstance.updatedCustomerInfoListener = null }
    }

    private val sharedPremiumStatusFlow: Flow<PremiumStatus> = combine(
        customerInfoFlow,
        grantedPremiumFlow,
    ) { customerInfo, grantedInfo -> toPremiumStatus(customerInfo, grantedInfo) }
        .distinctUntilChanged()
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1,
        )

    override fun observePremiumStatus(): Flow<PremiumStatus> = sharedPremiumStatusFlow

    override fun observeIsPremium(): Flow<Boolean> =
        sharedPremiumStatusFlow.map { it.isActive }.distinctUntilChanged()

    override suspend fun isPremiumNow(): Boolean {
        val customerInfo = runCatching { awaitCustomerInfo() }.getOrNull()
        return toPremiumStatus(customerInfo, grantedPremiumFlow.value).isActive
    }

    override suspend fun restore(): Boolean = runCatching {
        Purchases.sharedInstance.awaitRestore().entitlements[PREMIUM_ENTITLEMENT_ID]?.isActive == true
    }.getOrDefault(false)

    override suspend fun identify(userId: String) {
        runCatching { awaitLogIn(userId) }
        refreshGrantedPremium()
    }

    override suspend fun resetIdentity() {
        runCatching { awaitLogOut() }
        grantedPremiumFlow.value = GrantedPremiumInfo.None
    }

    private suspend fun refreshGrantedPremium() {
        grantedPremiumFlow.value =
            runCatching { grantedPremiumDataSource.fetchGrantedPremium() }.getOrDefault(GrantedPremiumInfo.None)
    }

    private fun toPremiumStatus(customerInfo: CustomerInfo?, grantedInfo: GrantedPremiumInfo): PremiumStatus {
        val entitlement = customerInfo?.entitlements?.get(PREMIUM_ENTITLEMENT_ID)
        return when {
            entitlement?.isActive == true -> entitlement.toPremiumStatus()
            grantedInfo.isGranted -> PremiumStatus(
                isActive = true,
                source = PremiumSource.GRANT,
                grantedUntil = grantedInfo.grantedUntil,
                grantReason = grantedInfo.grantReason,
            )

            else -> PremiumStatus.None
        }
    }

    private fun EntitlementInfo.toPremiumStatus(): PremiumStatus = PremiumStatus(
        isActive = true,
        source = PremiumSource.PURCHASE,
        expirationDate = expirationDate?.let { Instant.fromEpochMilliseconds(it.time) },
        willRenew = willRenew,
        periodType = periodType.toDomain(),
        planId = productPlanIdentifier ?: productIdentifier,
    )

    private fun PeriodType.toDomain(): PremiumPeriodType = when (this) {
        PeriodType.TRIAL -> PremiumPeriodType.TRIAL
        PeriodType.INTRO -> PremiumPeriodType.INTRO
        PeriodType.NORMAL -> PremiumPeriodType.NORMAL
        PeriodType.PREPAID -> PremiumPeriodType.PREPAID
    }

    private suspend fun awaitCustomerInfo(): CustomerInfo = suspendCancellableCoroutine { continuation ->
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                continuation.resumeWith(Result.success(customerInfo))
            }

            override fun onError(error: PurchasesError) {
                continuation.resumeWith(Result.failure(PurchasesException(error)))
            }
        })
    }

    private suspend fun awaitLogIn(userId: String): CustomerInfo = suspendCancellableCoroutine { continuation ->
        Purchases.sharedInstance.logIn(
            newAppUserID = userId,
            callback = object : LogInCallback {
                override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                    continuation.resumeWith(Result.success(customerInfo))
                }

                override fun onError(error: PurchasesError) {
                    continuation.resumeWith(Result.failure(PurchasesException(error)))
                }
            },
        )
    }

    private suspend fun awaitLogOut(): CustomerInfo = suspendCancellableCoroutine { continuation ->
        Purchases.sharedInstance.logOut(
            callback = object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    continuation.resumeWith(Result.success(customerInfo))
                }

                override fun onError(error: PurchasesError) {
                    continuation.resumeWith(Result.failure(PurchasesException(error)))
                }
            },
        )
    }

    private companion object {
        const val FOREGROUND_REFRESH_THROTTLE_MILLIS = 60_000L
    }
}

/** Fire-and-forget refresh: the shared [UpdatedCustomerInfoListener] propagates the result. */
private object NoopReceiveCustomerInfoCallback : ReceiveCustomerInfoCallback {
    override fun onReceived(customerInfo: CustomerInfo) = Unit
    override fun onError(error: PurchasesError) = Unit
}
