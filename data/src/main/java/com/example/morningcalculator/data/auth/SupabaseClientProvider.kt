package com.example.morningcalculator.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {
    fun create(url: String, key: String): SupabaseClient =
        createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
            install(Auth) {
                flowType = FlowType.PKCE
                // autoLoadFromStorage and alwaysAutoRefresh are on by default: the client
                // restores and refreshes the session on its own.
            }
            install(Postgrest)
        }
}
