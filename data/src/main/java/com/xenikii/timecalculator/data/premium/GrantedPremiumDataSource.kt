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
    suspend fun fetchIsGranted(): Boolean =
        client.from(PROFILES_TABLE)
            .select()
            .decodeSingleOrNull<RemoteProfile>()
            ?.isCurrentlyGranted()
            ?: false

    private fun RemoteProfile.isCurrentlyGranted(): Boolean {
        if (!grantedPremium) return false
        val until = grantedUntil ?: return true
        val expiresAt = runCatching { Instant.parse(until) }.getOrNull() ?: return true
        return expiresAt > Instant.fromEpochMilliseconds(System.currentTimeMillis())
    }

    private companion object {
        const val PROFILES_TABLE = "profiles"
    }
}

@Serializable
private data class RemoteProfile(
    @SerialName("granted_premium") val grantedPremium: Boolean = false,
    @SerialName("granted_until") val grantedUntil: String? = null,
)
