package com.xenikii.timecalculator.shared.components

import androidx.compose.foundation.Image
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.navigator.LocalNavigator

@Composable
fun BackButton(
    color: Color = MaterialTheme.colorScheme.onBackground,
    overrideOnBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    val canHandleBack = overrideOnBack != null || navigator.canNavigateBack

    IconButton(
        modifier = modifier,
        enabled = canHandleBack,
        onClick = { if (overrideOnBack != null) overrideOnBack() else navigator.navigateBack() }
    ) {
        Image(
            painterResource(R.drawable.back_arrow),
            contentDescription = stringResource(R.string.content_desc_back),
            colorFilter = ColorFilter.tint(color)
        )
    }
}