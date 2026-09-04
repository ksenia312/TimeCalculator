package com.xenikii.timecalculator.features.landing.ui.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.features.landing.presentation.LandingRoutineState
import kotlinx.coroutines.launch

@Composable
fun LandingCardPager(
    modifier: Modifier = Modifier,
    routineStates: List<LandingRoutineState>,
    onNavigate: (routineId: String) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { routineStates.size })
    val showTabs = routineStates.size > 1
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        if (showTabs) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                routineStates.forEachIndexed { index, routineState ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = {
                            Text(
                                text = routineState.cardViewItem.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        },
                    )
                }
            }
        }
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 12.dp),
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
    }
}
