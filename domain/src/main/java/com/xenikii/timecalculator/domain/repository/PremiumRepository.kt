package com.xenikii.timecalculator.domain.repository

import kotlinx.coroutines.flow.Flow

/** Free-tier gate limits and the RevenueCat entitlement identifier that unlocks them. */
const val FREE_ROUTINE_LIMIT = 3
const val FREE_TASK_LIMIT = 10
const val PREMIUM_ENTITLEMENT_ID = "premium"

interface PremiumRepository {
    fun observeIsPremium(): Flow<Boolean>
    suspend fun isPremiumNow(): Boolean
    suspend fun restore(): Boolean
    suspend fun identify(userId: String)
    suspend fun resetIdentity()
}
