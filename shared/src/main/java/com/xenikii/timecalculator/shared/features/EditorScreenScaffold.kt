package com.xenikii.timecalculator.shared.features

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.components.AddNewButton
import com.xenikii.timecalculator.shared.components.AppScaffold
import com.xenikii.timecalculator.shared.components.AppTextField
import com.xenikii.timecalculator.shared.components.BackButton
import com.xenikii.timecalculator.shared.components.SmallIconButton
import com.xenikii.timecalculator.shared.theme.LocalCustomColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreenScaffold(
    screenTitle: String,
    onDismiss: () -> Unit,
    headerActions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    AppScaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(
                        overrideOnBack = onDismiss,
                    )
                },
                title = {
                    Text(
                        screenTitle,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = headerActions,
            )
        },
    ) { padding ->
        content(padding)
    }
}

@Composable
fun TaskNameField(
    title: String,
    autofocus: Boolean,
    onValueChange: (String) -> Unit,
) {
    AppTextField(
        autofocus = autofocus,
        value = title,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.label_name)) },
    )
}

@Composable
fun DurationRow(
    index: Int,
    value: DurationInput,
    selectable: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
    onValueChange: (DurationInput) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (selectable) {
            RadioButton(
                modifier = Modifier.size(32.dp),
                selected = selected,
                onClick = onSelect,
            )
        }

        DurationFields(
            label = stringResource(R.string.label_duration_number, index + 1),
            value = value,
            onValueChange = onValueChange,
        )

        RemoveDurationButton(onClick = onRemove)
    }
}

@Composable
fun RowScope.DurationFields(
    label: String,
    value: DurationInput,
    onValueChange: (DurationInput) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .weight(1f)
            .offset(y = (-4).dp),
    ) {
        AppTextField(
            value = value.hours,
            onValueChange = { newValue ->
                onValueChange(value.copy(hours = newValue.filter(Char::isDigit)))
            },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.label_hours)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
        )

        AppTextField(
            value = value.minutes,
            onValueChange = { newValue ->
                onValueChange(value.copy(minutes = newValue.filter(Char::isDigit)))
            },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.label_minutes)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
        )
    }
}

@Composable
fun RemoveDurationButton(onClick: () -> Unit) {
    SmallIconButton(onClick = onClick) {
        Image(
            painter = painterResource(R.drawable.close),
            contentDescription = stringResource(R.string.content_desc_close),
        )
    }
}

@Composable
fun AddDurationButton(
    text: String,
    onClick: () -> Unit,
) {
    AddNewButton(
        text = text,
        foregroundColor = LocalCustomColorScheme.current.accent,
        modifier = Modifier.padding(top = 8.dp),
    ) {
        onClick()
    }
}

@Composable
fun SaveTaskButton(
    enabled: Boolean,
    onConfirm: () -> Unit,
) {
    ElevatedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        enabled = enabled,
        onClick = onConfirm,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(stringResource(R.string.action_save))
    }
}

fun selectedIndexAfterRemove(
    current: Int?,
    removedIndex: Int,
    newLastIndex: Int,
): Int? {
    if (current == null) return null

    return when {
        current == removedIndex -> newLastIndex
        current > removedIndex -> current - 1
        else -> current.coerceAtMost(newLastIndex)
    }
}