package com.xenikii.timecalculator.features.landing.ui.card

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.features.landing.presentation.LandingRoutineState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

private val TabsTopPadding = 16.dp
private val DefaultTopSpacing = 16.dp
private val TabsBlurRadius = 5.dp
private const val TabsTintAlpha = 0.9f

@Composable
fun LandingCardPager(
    modifier: Modifier = Modifier,
    routineStates: List<LandingRoutineState>,
    onNavigate: (routineId: String) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { routineStates.size })
    val showTabs = routineStates.size > 1
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val hazeState = rememberHazeState()
    var tabsHeight by remember { mutableStateOf(0.dp) }
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 10.dp,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
        ) { page ->
            LandingCard(
                routineState = routineStates[page],
                onNavigate = onNavigate,
                topSpacing = if (showTabs) tabsHeight + DefaultTopSpacing else DefaultTopSpacing,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showTabs) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { tabsHeight = with(density) { it.height.toDp() } }
                    .hazeEffect(state = hazeState) {
                        style = HazeStyle(
                            backgroundColor = backgroundColor,
                            tint = HazeTint(backgroundColor.copy(alpha = TabsTintAlpha)),
                            blurRadius = TabsBlurRadius,
                        )
                    }
            ) {
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    routineStates.forEachIndexed { index, routineState ->
                        LandingTab(
                            modifier = Modifier.padding(top = TabsTopPadding),
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
        }
    }
}

/**
 * A tab without the built-in ripple/press indication of [androidx.compose.material3.Tab] — the
 * blurred haze background makes any press state look muddy, so selection is only communicated via
 * [PrimaryTabRow]'s indicator.
 */
@Composable
private fun LandingTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
            )
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        text()
    }
}
