package com.xenikii.timecalculator.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.theme.TimeCalculatorTheme

@Composable
fun AppTextIconButton(
    painter: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    foregroundColor: Color = MaterialTheme.colorScheme.primary
) {
    AppTextButtonMedium(
        colors = ButtonDefaults.outlinedButtonColors(contentColor = foregroundColor),
        modifier = modifier.fillMaxWidth(), onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter,
                contentDescription = "",
                colorFilter = ColorFilter.tint(foregroundColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                modifier = Modifier.weight(weight = 1f, fill = false),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = foregroundColor
                ),
            )
        }
    }
}

@Composable
fun AddNewButton(
    text: String,
    modifier: Modifier = Modifier,
    foregroundColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    AppTextIconButton(
        foregroundColor = foregroundColor,
        painter = painterResource(R.drawable.add_circle),
        text = text,
        onClick = onClick,
        modifier = modifier
    )
}

@Preview
@Composable
fun AppTextIconButtonPreview() {
    TimeCalculatorTheme {
        Surface {
            AddNewButton(
                text = "Add New",
                onClick = {}
            )
        }
    }
}


@Preview
@Composable
fun AppTextIconButtonLongPreview() {
    TimeCalculatorTheme {
        Surface {
            AddNewButton(
                text = "Добавить еще продолжительность ",
                onClick = {}
            )
        }
    }
}