package com.xenikii.timecalculator.features.landing.ui.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.features.landing.presentation.LandingRoutineState

@Composable
fun LandingCardPager(
    modifier: Modifier = Modifier,
    routineStates: List<LandingRoutineState>,
    onNavigate: (routineId: String) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { routineStates.size })
    val showDots = routineStates.size > 1

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            LandingCard(
                routineState = routineStates[page],
                onNavigate = onNavigate,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showDots) {
            LandingCardPagerDots(
                count = routineStates.size,
                activeIndex = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp),
            )
        }
    }
}