package com.xenikii.timecalculator.features.auth.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.shared.components.AppTextField

@Composable
fun AuthPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelResId: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        enabled = enabled,
        isError = isError,
        label = { Text(stringResource(labelResId)) },
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible }
            ) {
                Icon(
                    imageVector = if (isPasswordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    contentDescription = stringResource(
                        if (isPasswordVisible) {
                            R.string.content_desc_hide_password
                        } else {
                            R.string.content_desc_show_password
                        }
                    )
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}
