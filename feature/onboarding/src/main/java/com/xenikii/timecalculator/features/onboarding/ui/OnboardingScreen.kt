package com.xenikii.timecalculator.features.onboarding.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.xenikii.timecalculator.features.onboarding.presentation.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val requestNotifications = rememberLauncherForActivityResult(
        contract = RequestPermission(),
    ) {
        viewModel.completeOnboarding()
        onFinished()
    }

    fun completeOnboarding() {
        viewModel.completeOnboarding()
        onFinished()
    }

    fun finishWithNotificationPrompt() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            completeOnboarding()
            return
        }

        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            completeOnboarding()
        } else {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    OnboardingContent(
        state = state,
        onPageChange = viewModel::onPageChange,
        onSkipClick = viewModel::onSkipClick,
        onNextClick = viewModel::onNextClick,
        onSkipNotificationsClick = ::completeOnboarding,
        onAllowNotificationsClick = ::finishWithNotificationPrompt,
    )
}
