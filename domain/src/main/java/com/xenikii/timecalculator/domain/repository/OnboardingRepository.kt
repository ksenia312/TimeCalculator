package com.xenikii.timecalculator.domain.repository

interface OnboardingRepository {
    fun isCompleted(): Boolean
    fun completeOnboarding()
}

interface OnboardingLocalDataSource {
    fun isCompleted(): Boolean
    fun setCompleted(completed: Boolean)
}
