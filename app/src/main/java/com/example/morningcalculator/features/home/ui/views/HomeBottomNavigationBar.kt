package com.example.morningcalculator.features.home.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Done
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme
import com.example.morningcalculator.shared.theme.PreviewAll
import com.example.morningcalculator.shared.theme.PreviewTheme

const val BOTTOM_BAR_MAX_HEIGHT = 120

enum class HomeTab(
    val label: String,
    val icon: ImageVector,
) {
    ROUTINES("Routines", Icons.AutoMirrored.Outlined.List), TASKS("Tasks", Icons.Outlined.Done),
}

@Composable
fun HomeBottomNavigationBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
    centerButton: @Composable () -> Unit = {},
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val (barRef, fabRef) = createRefs()

        Surface(
            modifier = Modifier
                .constrainAs(barRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(50.dp),
                    clip = false,
                )
                .clip(RoundedCornerShape(50.dp))
        ) {
            NavigationBar(
                tonalElevation = 10.dp,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                HomeTab.entries.forEach { tab ->
                    val selected = tab == selectedTab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onTabSelected(tab) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = {
                            Text(
                                tab.label,
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
                        ),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .constrainAs(fabRef) {
                    bottom.linkTo(barRef.top, margin = -(80).dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            centerButton()
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
                centerButton = {
                    Box(
                        Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {}
                }
            )
        }
    }
}