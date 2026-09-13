package com.xenikii.timecalculator.domain.model

import kotlin.time.Instant

/**
 * Consolidated premium status: what feeds the free-tier gates ([isActive]) plus enough detail for
 * the Settings UI to explain *how* premium is active, without leaking RevenueCat types into
 * :domain. [expirationDate] is `null` for a lifetime (non-expiring) purchase.
 */
data class PremiumStatus(
    val isActive: Boolean,
    val source: PremiumSource,
    val expirationDate: Instant? = null,
    val willRenew: Boolean? = null,
    val periodType: PremiumPeriodType? = null,
    val planId: String? = null,
    val grantedUntil: Instant? = null,
    val grantReason: String? = null,
) {
    companion object {
        val None = PremiumStatus(isActive = false, source = PremiumSource.NONE)
    }
}

enum class PremiumSource {
    /** Unlocked through a real store purchase/subscription (RevenueCat entitlement). */
    PURCHASE,

    /** Unlocked through a manual grant in `public.profiles` (gift/founder/promo). */
    GRANT,

    /** Not premium. */
    NONE,
}

enum class PremiumPeriodType {
    TRIAL,
    INTRO,
    NORMAL,
    PREPAID,
}
