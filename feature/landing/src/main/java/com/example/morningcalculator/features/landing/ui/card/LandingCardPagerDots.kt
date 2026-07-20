package com.example.morningcalculator.features.landing.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme

@Composable
fun LandingCardPagerDots(
    count: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier,
) {
    val inactive = LocalCustomColorScheme.current.unselected
    val active = MaterialTheme.colorScheme.primary

    if (count <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val isActive = index == activeIndex
            Box(
                modifier = Modifier
                    .size(if (isActive) 8.dp else 6.dp)
                    .background(
                        color = if (isActive) active else inactive,
                        shape = CircleShape
                    )
            )
        }
    }
}