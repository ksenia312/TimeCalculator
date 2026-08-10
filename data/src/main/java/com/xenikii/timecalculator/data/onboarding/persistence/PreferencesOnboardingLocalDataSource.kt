package com.xenikii.timecalculator.data.onboarding.persistence

import android.content.Context
import com.xenikii.timecalculator.domain.repository.OnboardingLocalDataSource

class PreferencesOnboardingLocalDataSource(
    context: Context,
) : OnboardingLocalDataSource {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isCompleted(): Boolean = prefs.getBoolean(KEY_COMPLETED, false)

    override fun setCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_COMPLETED, completed).apply()
    }

    private companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_COMPLETED = "completed"
    }
}
