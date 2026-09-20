package com.xenikii.timecalculator.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Tracks whether the user has already acknowledged the routine-limit-resolution screen (chosen
 * which routines stay active while premium is expired and they have more than
 * [FREE_ROUTINE_LIMIT] non-manually-paused routines), so the app doesn't force them back to that
 * screen on every app entry once they've made their choice.
 *
 * Reset to false the moment the underlying conflict actually goes away (premium returns, or they
 * delete enough routines) - not on every save - so a future recurrence of the same conflict
 * prompts again instead of staying silently acknowledged forever.
 */
interface RoutineLimitResolutionRepository {
    fun observeAcknowledged(): Flow<Boolean>
    suspend fun setAcknowledged(acknowledged: Boolean)
}
