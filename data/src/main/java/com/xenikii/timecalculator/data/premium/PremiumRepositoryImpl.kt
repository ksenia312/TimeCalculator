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
import com.xenikii.timecalculator.domain.model.PremiumEntitlementState
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
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

    // null = not checked yet (distinct from GrantedPremiumInfo.None = checked, no grant).
    private val grantedPremiumFlow = MutableStateFlow<GrantedPremiumInfo?>(null)

    @Volatile
    private var lastForegroundRefreshAtMillis = 0L

    @Volatile
    private var cachedEntitlementState: PremiumEntitlementState = PremiumEntitlementState.UNKNOWN

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
        android.util.Log.d("PREMIUM_DEBUG", "customerInfoFlow: registering listener + firing getCustomerInfo() @ ${System.currentTimeMillis()}")
        val listener = UpdatedCustomerInfoListener { info ->
            android.util.Log.d("PREMIUM_DEBUG", "customerInfoFlow: updatedCustomerInfoListener fired, appUserID=${info.originalAppUserId}, hasPremiumEntitlement=${info.entitlements[PREMIUM_ENTITLEMENT_ID]?.isActive} @ ${System.currentTimeMillis()}")
            trySend(info)
        }
        Purchases.sharedInstance.updatedCustomerInfoListener = listener
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                android.util.Log.d("PREMIUM_DEBUG", "customerInfoFlow: getCustomerInfo() onReceived, appUserID=${customerInfo.originalAppUserId}, hasPremiumEntitlement=${customerInfo.entitlements[PREMIUM_ENTITLEMENT_ID]?.isActive} @ ${System.currentTimeMillis()}")
                trySend(customerInfo)
            }

            override fun onError(error: PurchasesError) {
                android.util.Log.d("PREMIUM_DEBUG", "customerInfoFlow: getCustomerInfo() onError=$error @ ${System.currentTimeMillis()}")
                // No emission on failure; the listener above still delivers updates once
                // customer info becomes available (e.g. once connectivity returns).
            }
        })
        awaitClose { Purchases.sharedInstance.updatedCustomerInfoListener = null }
    }

    // Single shared subscription to customerInfoFlow; keeps the raw nullable grant state so
    // observeEntitlementState() can tell "not checked" apart from "checked, none".
    private val sharedRawStateFlow: Flow<Pair<CustomerInfo, GrantedPremiumInfo?>> = combine(
        customerInfoFlow,
        grantedPremiumFlow,
    ) { customerInfo, grantedInfo -> customerInfo to grantedInfo }
        .distinctUntilChanged()
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1,
        )

    private val sharedPremiumStatusFlow: Flow<PremiumStatus> = sharedRawStateFlow
        .map { (customerInfo, grantedInfo) -> toPremiumStatus(customerInfo, grantedInfo ?: GrantedPremiumInfo.None) }
        .distinctUntilChanged()

    init {
        // Keeps sharedRawStateFlow permanently hot and cachedEntitlementState current regardless
        // of whether any other collector happens to be subscribed.
        scope.launch { observeEntitlementState().collect { cachedEntitlementState = it } }
    }

    override fun observePremiumStatus(): Flow<PremiumStatus> = sharedPremiumStatusFlow

    override fun observeIsPremium(): Flow<Boolean> =
        sharedPremiumStatusFlow.map { it.isActive }.distinctUntilChanged()

    override fun observeEntitlementState(): Flow<PremiumEntitlementState> =
        sharedRawStateFlow
            .map { (customerInfo, grantedInfo) ->
                val hasActiveRcEntitlement = customerInfo.entitlements[PREMIUM_ENTITLEMENT_ID]?.isActive == true
                val state = when {
                    hasActiveRcEntitlement -> PremiumEntitlementState.ACTIVE
                    grantedInfo == null -> PremiumEntitlementState.UNKNOWN // grant not checked yet
                    grantedInfo.isGranted -> PremiumEntitlementState.ACTIVE
                    else -> PremiumEntitlementState.EXPIRED
                }
                android.util.Log.d("PREMIUM_DEBUG", "observeEntitlementState: hasActiveRcEntitlement=$hasActiveRcEntitlement, grantedInfo=$grantedInfo -> $state @ ${System.currentTimeMillis()}")
                state
            }
            .onStart {
                android.util.Log.d("PREMIUM_DEBUG", "observeEntitlementState: onStart emitting UNKNOWN @ ${System.currentTimeMillis()}")
                emit(PremiumEntitlementState.UNKNOWN)
            }
            .distinctUntilChanged()
            .onEach { android.util.Log.d("PREMIUM_DEBUG", "observeEntitlementState: downstream sees $it @ ${System.currentTimeMillis()}") }

    override suspend fun isPremiumNow(): Boolean {
        val customerInfo = runCatching { awaitCustomerInfo() }.getOrNull()
        if (customerInfo?.entitlements?.get(PREMIUM_ENTITLEMENT_ID)?.isActive == true) return true

        // Grant may not have been checked yet (e.g. right after cold start) - wait for a real
        // answer instead of treating "not checked" as "no grant", bounded so a fully offline
        // fetch (which resolves to None via refreshGrantedPremium's own getOrDefault) can't hang
        // this forever.
        val grantedInfo = withTimeoutOrNull(GRANT_CHECK_TIMEOUT_MILLIS) {
            grantedPremiumFlow.first { it != null }
        }
        return toPremiumStatus(customerInfo, grantedInfo ?: GrantedPremiumInfo.None).isActive
    }

    override fun isPremiumCached(): Boolean? = when (cachedEntitlementState) {
        PremiumEntitlementState.ACTIVE -> true
        PremiumEntitlementState.EXPIRED -> false
        PremiumEntitlementState.UNKNOWN -> null
    }

    override suspend fun restore(): Boolean = runCatching {
        Purchases.sharedInstance.awaitRestore().entitlements[PREMIUM_ENTITLEMENT_ID]?.isActive == true
    }.getOrDefault(false)

    override suspend fun identify(userId: String) {
        android.util.Log.d("PREMIUM_DEBUG", "identify($userId): start @ ${System.currentTimeMillis()}")
        // Any grant state held right now belongs to the previous identity (typically None from
        // resetIdentity() while logged out). Keeping it would pair the new user's RevenueCat info
        // (emitted by logIn below) with a stale "checked, no grant" and briefly report EXPIRED
        // for a Supabase-granted user - flashing the routine-limit-resolution screen and letting
        // RoutineAutoPauseCoordinator pause routines. Back to "not checked" = UNKNOWN until the
        // grant is actually fetched for this user; a failed fetch keeps it UNKNOWN, never EXPIRED.
        grantedPremiumFlow.value = null
        runCatching { awaitLogIn(userId) }
        android.util.Log.d("PREMIUM_DEBUG", "identify($userId): awaitLogIn done, calling refreshGrantedPremium @ ${System.currentTimeMillis()}")
        refreshGrantedPremium()
        android.util.Log.d("PREMIUM_DEBUG", "identify($userId): refreshGrantedPremium done, grantedPremiumFlow.value=${grantedPremiumFlow.value} @ ${System.currentTimeMillis()}")
    }

    override suspend fun resetIdentity() {
        android.util.Log.d("LOGOUT_DEBUG", "PremiumRepositoryImpl.resetIdentity(): calling awaitLogOut() @ ${System.currentTimeMillis()}")
        runCatching { awaitLogOut() }
        android.util.Log.d("LOGOUT_DEBUG", "PremiumRepositoryImpl.resetIdentity(): awaitLogOut() done @ ${System.currentTimeMillis()}")
        grantedPremiumFlow.value = GrantedPremiumInfo.None
    }

    // On failure, deliberately leaves grantedPremiumFlow untouched instead of defaulting to
    // GrantedPremiumInfo.None - a fetch error (network blip, token-refresh race, RLS not ready
    // yet) is not the same signal as "checked, no grant", and collapsing it to None was
    // downgrading legitimately-granted users to EXPIRED until the next app restart. Mirrors
    // customerInfoFlow's onError, which likewise never emits a negative result on failure.
    private suspend fun refreshGrantedPremium() {
        runCatching { grantedPremiumDataSource.fetchGrantedPremium() }
            .onSuccess { grantedPremiumFlow.value = it }
            .onFailure { error ->
                android.util.Log.d(
                    "PREMIUM_DEBUG",
                    "refreshGrantedPremium: fetch failed, keeping last known grantedPremiumFlow=${grantedPremiumFlow.value} @ ${System.currentTimeMillis()}",
                    error,
                )
            }
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
        const val GRANT_CHECK_TIMEOUT_MILLIS = 5_000L
    }
}

/** Fire-and-forget refresh: the shared [UpdatedCustomerInfoListener] propagates the result. */
private object NoopReceiveCustomerInfoCallback : ReceiveCustomerInfoCallback {
    override fun onReceived(customerInfo: CustomerInfo) = Unit
    override fun onError(error: PurchasesError) = Unit
}
