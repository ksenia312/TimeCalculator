package com.xenikii.timecalculator.data.onboarding

import com.xenikii.timecalculator.domain.repository.OnboardingLocalDataSource
import com.xenikii.timecalculator.domain.repository.OnboardingRepository

class OnboardingRepositoryImpl(
    private val localDataSource: OnboardingLocalDataSource,
) : OnboardingRepository {

    override fun isCompleted(): Boolean = localDataSource.isCompleted()

    override fun completeOnboarding() {
        localDataSource.setCompleted(completed = true)
    }
}
