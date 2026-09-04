package com.xenikii.timecalculator.shared.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.shared.preview.PreviewAll
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
    defaultTextAlign: TextAlign = TextAlign.Center,
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
            CompositionLocalProvider(LocalTextStyle provides defaultTextStyle.copy(textAlign = defaultTextAlign)) {
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
    defaultTextAlign: TextAlign = TextAlign.Center,
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
            CompositionLocalProvider(LocalTextStyle provides defaultTextStyle.copy(textAlign = defaultTextAlign)) {
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
    defaultTextAlign: TextAlign = TextAlign.Center,
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
            CompositionLocalProvider(LocalTextStyle provides defaultTextStyle.copy(textAlign = defaultTextAlign)) {
                content()
            }
        },
    )

}

@Composable
fun AppTextButtonMedium(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = AppButtonShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        focusedElevation = 0.dp,
        disabledElevation = 0.dp,
        hoveredElevation = 0.dp,
    ),
    contentPadding: PaddingValues = AppButtonContentMediumPadding,
    defaultTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    defaultTextAlign: TextAlign = TextAlign.Center,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = {
            CompositionLocalProvider(LocalTextStyle provides defaultTextStyle.copy(textAlign = defaultTextAlign)) {
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
            ) {
                Text("Primary")
            }
        }
    }
}

@Preview
@PreviewAll
@Composable
private fun AppButtonMediumPreview() {
    TimeCalculatorTheme {
        Surface {
            AppButtonMedium(
                onClick = {},
            ) {
                Text("P")
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
            ) {
                Text("Elevated Medium")
            }
        }
    }
}

@Preview
@Composable
private fun AppTextButtonMediumPreview() {
    TimeCalculatorTheme {
        Surface {
            AppTextButtonMedium(
                onClick = {},
            ) {
                Text("Outlined Medium")
            }
        }
    }
}
