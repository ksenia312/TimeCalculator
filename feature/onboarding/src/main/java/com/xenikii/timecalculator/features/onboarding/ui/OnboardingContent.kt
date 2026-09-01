package com.xenikii.timecalculator.features.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.onboarding.presentation.OnboardingViewState
import com.xenikii.timecalculator.features.onboarding.presentation.onboardingImages
import com.xenikii.timecalculator.features.onboarding.ui.components.OnboardingImagePage
import com.xenikii.timecalculator.features.onboarding.ui.components.OnboardingNotificationPage
import com.xenikii.timecalculator.features.onboarding.ui.components.OnboardingPagerDots
import com.xenikii.timecalculator.shared.components.AppButtonMedium
import com.xenikii.timecalculator.shared.components.AppScaffold
import com.xenikii.timecalculator.shared.components.AppTextButtonMedium
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

@Composable
fun OnboardingContent(
    state: OnboardingViewState,
    onPageChange: (Int) -> Unit,
    onSkipClick: () -> Unit,
    onNextClick: () -> Unit,
    onSkipNotificationsClick: () -> Unit,
    onAllowNotificationsClick: () -> Unit,
) {
    val pageCount = remember { onboardingImages.size + 1 }
    val lastPageIndex = pageCount - 1
    val pagerState = rememberPagerState(
        initialPage = state.currentPage,
        pageCount = { pageCount },
    )
    val isNotificationPage = pagerState.currentPage == lastPageIndex

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect(onPageChange)
    }

    LaunchedEffect(state.currentPage) {
        if (pagerState.currentPage != state.currentPage) {
            if (abs(pagerState.currentPage - state.currentPage) > 1) {
                pagerState.scrollToPage(state.currentPage)
            } else {
                pagerState.animateScrollToPage(state.currentPage)
            }
        }
    }

    AppScaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Spacer(Modifier.height(32.dp))
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 8.dp),
                pageSpacing = 12.dp,
                modifier = Modifier.weight(1f),
            ) { page ->
                if (page < onboardingImages.size) {
                    OnboardingImagePage(
                        imageRes = onboardingImages[page],
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    OnboardingNotificationPage(
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            OnboardingPagerDots(
                count = pageCount,
                activeIndex = pagerState.currentPage,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(16.dp))

            if (isNotificationPage) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppButtonMedium(
                        onClick = onAllowNotificationsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.onboarding_notifications_allow))
                    }
                    AppTextButtonMedium(
                        onClick = onSkipNotificationsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.onboarding_notifications_skip))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppButtonMedium(
                        onClick = onNextClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.onboarding_next))
                    }
                    AppTextButtonMedium(
                        onClick = onSkipClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.onboarding_skip))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@PreviewAll
@Composable
fun OnboardingContentPreview() {
    PreviewTheme {
        OnboardingContent(
            state = OnboardingViewState(),
            onPageChange = {},
            onSkipClick = {},
            onNextClick = {},
            onSkipNotificationsClick = {},
            onAllowNotificationsClick = {},
        )
    }
}

@PreviewAll
@Composable
fun OnboardingNotificationsPreview() {
    PreviewTheme {
        OnboardingContent(
            state = OnboardingViewState(currentPage = onboardingImages.size),
            onPageChange = {},
            onSkipClick = {},
            onNextClick = {},
            onSkipNotificationsClick = {},
            onAllowNotificationsClick = {},
        )
    }
}
