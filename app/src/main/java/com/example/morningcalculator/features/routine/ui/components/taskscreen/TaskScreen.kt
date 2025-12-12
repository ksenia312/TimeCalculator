package com.example.morningcalculator.features.routine.ui.components.taskscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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
fun <T> TaskScreen(
    screenTitle: String,
    data: List<T>,
    initialIndex: Int?,
    initialTitle: String,
    toInputValues: (List<T>) -> List<String>,
    onValueChange: (T, String) -> T,
    confirmEnabled: (List<T>) -> Boolean,
    onConfirm: (String, List<T>, Int?) -> Unit,
    onDismiss: () -> Unit,
    headerActions: @Composable () -> Unit = {},
    newElement: T,
) {
    FullScreenDialog(onDismiss) {
        AppScaffold(
            modifier = Modifier.imePadding(),
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                TopAppBar(navigationIcon = {
                    BackButton(
                        overrideOnBack = onDismiss
                    )
                }, title = {
                    Text(
                        screenTitle, style = MaterialTheme.typography.titleLarge
                    )
                }, actions = { headerActions() })
            }) { it ->
            var title by remember { mutableStateOf(initialTitle) }
            var selectedIndex by remember { mutableStateOf(initialIndex) }
            val scrollState = rememberScrollState()
            val mutableData = remember { data.toMutableStateList() }
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .scrollable(scrollState, orientation = Orientation.Vertical),
            ) {
                NameEditor(
                    title,
                    autofocus = initialTitle.isEmpty(),
                    onValueChange = { title = it })
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { clip = false },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val numbers = toInputValues(mutableData)
                    item { Spacer(Modifier.height(4.dp)) }
                    items(numbers.size) { index ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { clip = false },
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (selectedIndex != null) RadioButton(
                                modifier = Modifier.size(32.dp),
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index })
                            NumberField(
                                value = numbers[index], onValueChange = { new ->
                                    if (new.all { it.isDigit() }) {
                                        mutableData[index] = onValueChange(
                                            mutableData[index], new
                                        )
                                    }
                                }, label = "Duration №${index + 1}"
                            )
                            SmallIconButton(onClick = {
                                mutableData.removeAt(index)
                                selectedIndex = mutableData.lastIndex
                            }) {
                                Image(
                                    painterResource(R.drawable.close),
                                    contentDescription = "close",
                                )
                            }
                        }
                    }
                    item {
                        Box(modifier = Modifier.padding(top = 8.dp)) {
                            AddNewButton(
                                text = "Add more durations",
                                foregroundColor = LocalCustomColorScheme.current.accent
                            ) {
                                mutableData.add(newElement)
                                if (selectedIndex != null) {
                                    selectedIndex = mutableData.lastIndex
                                }
                            }
                        }
                    }
                }
                ConfirmButton(
                    enabled = confirmEnabled(mutableData), onConfirm = {
                        onConfirm(
                            title, mutableData, selectedIndex
                        )
                    }, onDismiss = onDismiss, modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )
            }
        }
    }
}

@Composable
private fun ConfirmButton(
    modifier: Modifier, onConfirm: () -> Unit, enabled: Boolean, onDismiss: () -> Unit
) = ElevatedButton(
    modifier = modifier, enabled = enabled, onClick = {
        onConfirm()
        onDismiss()
    }, colors = ButtonDefaults.elevatedButtonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) { Text("Save") }

@Composable
private fun NameEditor(
    title: String,
    autofocus: Boolean,
    onValueChange: (String) -> Unit,
) {
    AppTextField(
        autofocus = autofocus,
        value = title,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Name") })
}

@Composable
private fun RowScope.NumberField(
    value: String, onValueChange: (String) -> Unit, label: String, enabled: Boolean = true
) {
    AppTextField(
        value = value,
        enabled = enabled,
        onValueChange = onValueChange,
        modifier = Modifier
            .weight(1f)
            .offset(y = (-4).dp),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun FullScreenDialog(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            tonalElevation = 0.dp
        ) {
            content()
        }
    }
}