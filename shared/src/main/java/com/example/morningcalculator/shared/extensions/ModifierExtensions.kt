package com.example.morningcalculator.shared.extensions

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val BOTTOM_BAR_MAX_HEIGHT = 120

fun Modifier.bottomIndent(): Modifier = this.padding(bottom = 24.dp + BOTTOM_BAR_MAX_HEIGHT.dp)
