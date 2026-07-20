package com.example.morningcalculator.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme

const val BOTTOM_BAR_MAX_HEIGHT = 120

enum class HomeTab(
    val labelResId: Int,
    val iconRes: Int,
) {
    LANDING(R.string.home_tab_home, R.drawable.home),
    ROUTINES(R.string.home_tab_routines, R.drawable.routine),
    TASKS(R.string.home_tab_tasks, R.drawable.task),
}

@Composable
fun HomeBottomNavigationBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 20.dp /*vertical = 40.dp*/)
                .fillMaxWidth()
                .navigationBarsPadding()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(50.dp),
                    clip = false,
                )
                .clip(RoundedCornerShape(50.dp))
        ) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                HomeTab.entries.forEach { tab ->
                    val selected = tab == selectedTab
                    val label = stringResource(tab.labelResId)
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.iconRes),
                                contentDescription = label,
                            )
                        },
                        label = {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected)
                                        FontWeight.Bold
                                    else
                                        FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LocalCustomColorScheme.current.accent,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}

@PreviewAll
@Composable
fun HomeBottomNavigationBarPreview() {
    PreviewTheme {
        Box(Modifier.background(Color.Yellow)) {
            HomeBottomNavigationBar(
                selectedTab = HomeTab.ROUTINES,
                onTabSelected = {},
            )
        }
    }
}