package com.example.morningcalculator.data.sync.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class SupabaseRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun pushTasks(remoteTasks: List<RemoteTask>) {
        if (remoteTasks.isEmpty()) return
        supabaseClient.from("tasks").upsert(remoteTasks)
    }

    suspend fun pushRoutines(remoteRoutines: List<RemoteRoutine>) {
        if (remoteRoutines.isEmpty()) return
        supabaseClient.from("routines").upsert(remoteRoutines)
    }

    suspend fun pullTasks(
        updatedAtCursor: String?,
        limit: Int,
        offset: Int,
    ): List<RemoteTask> =
        supabaseClient.from("tasks").select {
            filter {
                if (updatedAtCursor != null) {
                    gte("updated_at", updatedAtCursor)
                }
            }
            order(column = "updated_at", order = Order.ASCENDING)
            order(column = "id", order = Order.ASCENDING)
            range(offset.toLong(), (offset + limit - 1).toLong())
        }.decodeList()

    suspend fun pullRoutines(
        updatedAtCursor: String?,
        limit: Int,
        offset: Int,
    ): List<RemoteRoutine> =
        supabaseClient.from("routines").select {
            filter {
                if (updatedAtCursor != null) {
                    gte("updated_at", updatedAtCursor)
                }
            }
            order(column = "updated_at", order = Order.ASCENDING)
            order(column = "id", order = Order.ASCENDING)
            range(offset.toLong(), (offset + limit - 1).toLong())
        }.decodeList()
}
