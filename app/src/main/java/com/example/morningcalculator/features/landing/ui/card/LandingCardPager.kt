package com.example.morningcalculator.features.landing.ui.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine

@Composable
fun LandingCardPager(
    modifier: Modifier = Modifier,
    routines: List<Routine>,
    onNavigate: (routine: Routine) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { routines.size })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            LandingCard(
                routine = routines[page], onNavigate = onNavigate, modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(12.dp))

        LandingCardPagerDots(
            count = routines.size,
            activeIndex = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )
    }
}

