package com.example.morningcalculator.shared.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.R
import com.example.morningcalculator.shared.extensions.stringDateTime
import com.example.morningcalculator.shared.preview.PreviewAll
import com.example.morningcalculator.shared.preview.PreviewConstants
import com.example.morningcalculator.shared.preview.PreviewTheme
import com.example.morningcalculator.shared.theme.ChangeSystemTopBarTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CustomTopBar(
    modifier: Modifier = Modifier,
    onAccentColor: Color,
    actions: @Composable RowScope.() -> Unit = {},
    showNavigationIcon: Boolean = false,
    content: @Composable () -> Unit,
) {
    ChangeSystemTopBarTheme(onAccentColor)

    Column(modifier.fillMaxWidth().statusBarsPadding()) {
        if (showNavigationIcon) Box(
            Modifier.align(Alignment.Start)
        ) {
            BackButton(
                color = onAccentColor
            )
        }

        Box(
            Modifier.align(Alignment.End)
        ) {
            Row { actions() }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            Modifier.padding(
                horizontal = 16.dp
            )
        ) {
            content()
        }
    }
}

@PreviewAll
@Composable
fun CustomTopBarPreview() {
    val routine = PreviewConstants.routinesFull.first()
    val context = LocalContext.current
    PreviewTheme {
        CustomTopBar(
            onAccentColor = Color.White,
            actions = {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.modified_at),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.End
                    )
                    Text(
                        routine.modifiedAt.stringDateTime(context),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
            },
            showNavigationIcon = true,
        ) {
            Text(stringResource(R.string.app_bar_preview_title))
        }
    }
}