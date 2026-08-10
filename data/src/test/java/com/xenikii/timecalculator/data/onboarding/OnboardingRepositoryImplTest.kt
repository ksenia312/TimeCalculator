package com.xenikii.timecalculator.data.onboarding

import com.xenikii.timecalculator.domain.repository.OnboardingLocalDataSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingRepositoryImplTest {

    @Test
    fun `isCompleted returns value from local data source`() {
        val repository = OnboardingRepositoryImpl(localDataSource = FakeOnboardingLocalDataSource(true))

        assertTrue(repository.isCompleted())
    }

    @Test
    fun `completeOnboarding marks onboarding as completed`() {
        val localDataSource = FakeOnboardingLocalDataSource(false)
        val repository = OnboardingRepositoryImpl(localDataSource = localDataSource)

        assertFalse(localDataSource.isCompleted())

        repository.completeOnboarding()

        assertTrue(localDataSource.isCompleted())
    }
}

private class FakeOnboardingLocalDataSource(
    private var completed: Boolean,
) : OnboardingLocalDataSource {

    override fun isCompleted(): Boolean = completed

    override fun setCompleted(completed: Boolean) {
        this.completed = completed
    }
}
