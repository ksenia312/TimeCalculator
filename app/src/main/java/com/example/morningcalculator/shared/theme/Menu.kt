package com.example.morningcalculator.shared.theme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.sp


class EmojiInfo(
    val emoji: String,
    val name: String,
    val description: String
)

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun FabMenu(
    modifier: Modifier = Modifier
) {

    val emojiList = List(100) { index ->
        EmojiInfo(
            emoji = when (index % 5) {
                0 -> "😀"
                1 -> "🎉"
                2 -> "🚀"
                3 -> "❤️"
                else -> "🔥"
            },
            name = "Emoji $index",
            description = "This is the description for emoji number $index."
        )
    }
    val listState = rememberLazyListState()

    // In the official Google samples, the recommandation is to use a BackHandler
//    BackHandler(isFabMenuExpanded) { isFabMenuExpanded = false }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            val emojiActions = listOf(
                "😀" to "Smile",
                "🎉" to "Celebrate",
                "🚀" to "Launch",
                "❤️" to "Love",
            )
            val isFabMenuVisible by remember {
                derivedStateOf {
                    listState.firstVisibleItemIndex == 0
                }
            }
            var isFabMenuExpanded by rememberSaveable { mutableStateOf(false) }


            FloatingActionButtonMenu(
                expanded = isFabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        modifier = Modifier
                            .animateFloatingActionButton(
                                visible = isFabMenuVisible || isFabMenuExpanded,
                                alignment = Alignment.BottomEnd
                            ),
                        checked = isFabMenuExpanded,
                        onCheckedChange = {
                            isFabMenuExpanded = !isFabMenuExpanded
                        }
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                            }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress })
                        )
                    }
                }
            ) {
                emojiActions.forEachIndexed { i, action ->
                    FloatingActionButtonMenuItem(
                        onClick = {
                            isFabMenuExpanded = false
                        },
                        icon = {
                            Text(
                                text = action.first,
                                fontSize = 24.sp
                            )
                        },
                        text = {
                            Text(
                                text = action.second,
                            )
                        },
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = listState,
        ) {
        }
    }
}
