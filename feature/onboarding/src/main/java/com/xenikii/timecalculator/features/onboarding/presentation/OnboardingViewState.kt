package com.xenikii.timecalculator.features.onboarding.presentation

import com.xenikii.timecalculator.R

internal val onboardingImages = listOf(
    R.drawable.onboarding0,
    R.drawable.onboarding1,
    R.drawable.onboarding2,
    R.drawable.onboarding3,
    R.drawable.onboarding4,
)

data class OnboardingViewState(
    val currentPage: Int = 0,
)
