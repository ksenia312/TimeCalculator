package com.xenikii.timecalculator.shared.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.shared.theme.TimeCalculatorTheme

val AppButtonShape = RoundedCornerShape(1000.dp)
val AppButtonContentLargePadding = PaddingValues(vertical = 44.dp, horizontal = 52.dp)
val AppButtonContentMediumPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)

@Composable
fun AppButtonExpressive(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AppButtonShape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: PaddingValues = AppButtonContentLargePadding,
    defaultTextStyle: TextStyle = MaterialTheme.typography.titleLarge,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = {
            CompositionLocalProvider(LocalTextStyle provides defaultTextStyle) {
                content()
            }
        },
    )
}

@Composable
fun AppButtonMedium(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AppButtonShape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: PaddingValues = AppButtonContentMediumPadding,
    defaultTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = {
            CompositionLocalProvider(LocalTextStyle provides defaultTextStyle) {
                content()
            }
        },
    )
}

@Composable
fun AppElevatedButtonMedium(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AppButtonShape,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.elevatedButtonElevation(),
    contentPadding: PaddingValues = AppButtonContentMediumPadding,
    defaultTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    content: @Composable RowScope.() -> Unit,
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = {
            CompositionLocalProvider(LocalTextStyle provides defaultTextStyle) {
                content()
            }
        },
    )
}

@Composable
fun AppOutlinedButtonMedium(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AppButtonShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    contentPadding: PaddingValues = AppButtonContentMediumPadding,
    defaultTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = {
            CompositionLocalProvider(LocalTextStyle provides defaultTextStyle) {
                content()
            }
        },
    )
}

@Preview
@Composable
private fun AppButtonExpressivePreview() {
    TimeCalculatorTheme {
        Surface {
            AppButtonExpressive(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Primary")
            }
        }
    }
}

@Preview
@Composable
private fun AppButtonMediumPreview() {
    TimeCalculatorTheme {
        Surface {
            AppButtonMedium(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Primary Medium")
            }
        }
    }
}

@Preview
@Composable
private fun AppElevatedButtonMediumPreview() {
    TimeCalculatorTheme {
        Surface {
            AppElevatedButtonMedium(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Elevated Medium")
            }
        }
    }
}

@Preview
@Composable
private fun AppOutlinedButtonMediumPreview() {
    TimeCalculatorTheme {
        Surface {
            AppOutlinedButtonMedium(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Outlined Medium")
            }
        }
    }
}
