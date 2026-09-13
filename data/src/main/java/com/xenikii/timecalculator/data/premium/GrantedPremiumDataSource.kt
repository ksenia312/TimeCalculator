package com.xenikii.timecalculator.data.premium

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Reads the manually-granted-premium flag from `public.profiles` - gifts/founder/promo access set
 * by hand in the Supabase dashboard, independent of RevenueCat. RLS already scopes the table to
 * the caller's own row, so no user id filter is needed here.
 */
class GrantedPremiumDataSource(
    private val client: SupabaseClient,
) {
    suspend fun fetchGrantedPremium(): GrantedPremiumInfo =
        client.from(PROFILES_TABLE)
            .select()
            .decodeSingleOrNull<RemoteProfile>()
            ?.toGrantedPremiumInfo()
            ?: GrantedPremiumInfo.None

    private fun RemoteProfile.toGrantedPremiumInfo(): GrantedPremiumInfo {
        if (!grantedPremium) return GrantedPremiumInfo.None
        val expiresAt = grantedUntil?.let { runCatching { Instant.parse(it) }.getOrNull() }
        val hasExpired = expiresAt != null && expiresAt <= Instant.fromEpochMilliseconds(System.currentTimeMillis())
        if (hasExpired) return GrantedPremiumInfo.None
        return GrantedPremiumInfo(isGranted = true, grantedUntil = expiresAt, grantReason = grantReason)
    }

    private companion object {
        const val PROFILES_TABLE = "profiles"
    }
}

/** Data-layer result of a grant lookup; mapped to the domain-clean `PremiumStatus` in [PremiumRepositoryImpl]. */
data class GrantedPremiumInfo(
    val isGranted: Boolean,
    val grantedUntil: Instant?,
    val grantReason: String?,
) {
    companion object {
        val None = GrantedPremiumInfo(isGranted = false, grantedUntil = null, grantReason = null)
    }
}

@Serializable
private data class RemoteProfile(
    @SerialName("granted_premium") val grantedPremium: Boolean = false,
    @SerialName("granted_until") val grantedUntil: String? = null,
    @SerialName("grant_reason") val grantReason: String? = null,
)
