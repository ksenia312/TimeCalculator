package com.xenikii.timecalculator.domain.repository

import com.xenikii.timecalculator.domain.model.PremiumEntitlementState
import com.xenikii.timecalculator.domain.model.PremiumStatus
import kotlinx.coroutines.flow.Flow

/** Free-tier gate limits and the RevenueCat entitlement identifier that unlocks them. */
const val FREE_ROUTINE_LIMIT = 3
const val FREE_TASK_LIMIT = 10
const val PREMIUM_ENTITLEMENT_ID = "premium"

interface PremiumRepository {
    /** The detailed status (source, expiration, renewal...) behind [observeIsPremium]. */
    fun observePremiumStatus(): Flow<PremiumStatus>

    /** Derived from [observePremiumStatus]; kept for gates that only care about the boolean. */
    fun observeIsPremium(): Flow<Boolean>
    suspend fun isPremiumNow(): Boolean

    /**
     * Like [observePremiumStatus] but starts with [PremiumEntitlementState.UNKNOWN] until
     * RevenueCat has actually answered at least once, for callers that must not treat "not
     * loaded yet" as "expired" (e.g. auto-pausing routines on expiry).
     */
    fun observeEntitlementState(): Flow<PremiumEntitlementState>

    /**
     * Last known premium state, readable synchronously (no suspend/network round-trip) for call
     * sites that can't await a fresh check, e.g. deciding notification content when posting.
     */
    fun isPremiumCached(): Boolean
    suspend fun restore(): Boolean
    suspend fun identify(userId: String)
    suspend fun resetIdentity()
}
