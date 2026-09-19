package com.xenikii.timecalculator.domain.model

/**
 * Tri-state view of premium entitlement, distinguishing a confirmed answer from RevenueCat not
 * having answered yet. [PremiumStatus]/`isPremiumNow()` collapse "not premium" and "not loaded
 * yet" into the same `false` - callers that must never act on a network-lag false negative (e.g.
 * auto-pausing routines) need this distinction instead.
 */
enum class PremiumEntitlementState {
    /** No confirmed answer yet (RevenueCat hasn't responded since process start). */
    UNKNOWN,
    ACTIVE,
    EXPIRED,
}
