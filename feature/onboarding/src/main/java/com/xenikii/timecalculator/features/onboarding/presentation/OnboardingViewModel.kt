package com.xenikii.timecalculator.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import com.xenikii.timecalculator.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingViewState())
    val state: StateFlow<OnboardingViewState> = _state.asStateFlow()

    fun onPageChange(page: Int) {
        _state.update { it.copy(currentPage = page) }
    }

    fun onSkipClick() {
        _state.update { it.copy(currentPage = onboardingImages.size) }
    }

    fun onNextClick() {
        _state.update { currentState ->
            currentState.copy(
                currentPage = (currentState.currentPage + 1).coerceAtMost(onboardingImages.size),
            )
        }
    }

    fun completeOnboarding() {
        onboardingRepository.completeOnboarding()
    }
}
