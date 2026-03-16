package com.example.morningcalculator.features.routine.ui.components.taskscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.morningcalculator.R
import com.example.morningcalculator.shared.components.AddNewButton
import com.example.morningcalculator.shared.components.AppScaffold
import com.example.morningcalculator.shared.components.AppTextField
import com.example.morningcalculator.shared.components.BackButton
import com.example.morningcalculator.shared.components.SmallIconButton
import com.example.morningcalculator.shared.theme.LocalCustomColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialogScaffold(
    screenTitle: String,
    onDismiss: () -> Unit,
    headerActions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    FullScreenDialog(onDismiss = onDismiss) {
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
        label = { Text("Name") },
    )
}

@Composable
fun DurationRow(
    index: Int,
    value: String,
    selectable: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (selectable) {
            RadioButton(
                modifier = Modifier.size(32.dp),
                selected = selected,
                onClick = onSelect,
            )
        }

        DurationField(
            value = value,
            label = "Duration №${index + 1}",
            onValueChange = onValueChange,
        )

        RemoveDurationButton(onClick = onRemove)
    }
}

@Composable
fun RowScope.DurationField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .weight(1f)
            .offset(y = (-4).dp),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        ),
    )
}

@Composable
fun RemoveDurationButton(onClick: () -> Unit) {
    SmallIconButton(onClick = onClick) {
        Image(
            painter = painterResource(R.drawable.close),
            contentDescription = "close",
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
    onDismiss: () -> Unit,
) {
    ElevatedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        enabled = enabled,
        onClick = {
            onConfirm()
            onDismiss()
        },
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text("Save")
    }
}

@Composable
fun FullScreenDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            tonalElevation = 0.dp,
        ) {
            content()
        }
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