package com.xenikii.timecalculator.data.premium

import android.content.Context
import androidx.core.content.edit
import com.xenikii.timecalculator.domain.repository.RoutineLimitResolutionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRoutineLimitResolutionRepository(
    context: Context,
) : RoutineLimitResolutionRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val acknowledgedState = MutableStateFlow(prefs.getBoolean(KEY_ACKNOWLEDGED, false))

    override fun observeAcknowledged(): Flow<Boolean> = acknowledgedState.asStateFlow()

    override suspend fun setAcknowledged(acknowledged: Boolean) {
        prefs.edit { putBoolean(KEY_ACKNOWLEDGED, acknowledged) }
        acknowledgedState.value = acknowledged
    }

    private companion object {
        const val PREFS_NAME = "routine_limit_resolution_prefs"
        const val KEY_ACKNOWLEDGED = "limit_resolution_acknowledged"
    }
}
