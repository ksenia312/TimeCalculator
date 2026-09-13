package com.xenikii.timecalculator.data.premium

import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
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
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Wraps [Purchases.sharedInstance] and [GrantedPremiumDataSource]: a user is premium if either
 * source says so. RevenueCat SDK 10.x only exposes coroutine `await*` extensions for
 * offerings/purchase/restore - [Purchases.getCustomerInfo], [Purchases.logIn] and
 * [Purchases.logOut] are still callback-only, so those three are bridged here manually with
 * [suspendCancellableCoroutine].
 */
class PremiumRepositoryImpl(
    private val scope: CoroutineScope,
    private val grantedPremiumDataSource: GrantedPremiumDataSource,
) : PremiumRepository {

    // Manually-granted premium (gifts/founder/promo) is read once per login/session start (see
    // identify()/resetIdentity()) and cached here - it's admin-set and changes rarely, so there's
    // no need to hit Supabase on every isPremiumNow()/observeIsPremium() check.
    private val grantedPremiumFlow = MutableStateFlow(false)

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

    private val sharedIsPremiumFlow: Flow<Boolean> = combine(
        customerInfoFlow.map { it.isPremium() },
        grantedPremiumFlow,
    ) { isRevenueCatPremium, isGrantedPremium -> isRevenueCatPremium || isGrantedPremium }
        .distinctUntilChanged()
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1,
        )

    override fun observeIsPremium(): Flow<Boolean> = sharedIsPremiumFlow

    override suspend fun isPremiumNow(): Boolean {
        val isRevenueCatPremium = runCatching { awaitCustomerInfo().isPremium() }.getOrDefault(false)
        return isRevenueCatPremium || grantedPremiumFlow.value
    }

    override suspend fun restore(): Boolean = runCatching {
        Purchases.sharedInstance.awaitRestore().isPremium()
    }.getOrDefault(false)

    override suspend fun identify(userId: String) {
        runCatching { awaitLogIn(userId) }
        grantedPremiumFlow.value = runCatching { grantedPremiumDataSource.fetchIsGranted() }.getOrDefault(false)
    }

    override suspend fun resetIdentity() {
        runCatching { awaitLogOut() }
        grantedPremiumFlow.value = false
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

    private fun CustomerInfo.isPremium(): Boolean =
        entitlements[PREMIUM_ENTITLEMENT_ID]?.isActive == true
}
