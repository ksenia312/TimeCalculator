package com.xenikii.timecalculator.features.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.auth.presentation.AuthFormState
import com.xenikii.timecalculator.shared.components.AppButtonExpressive
import com.xenikii.timecalculator.shared.components.AppScaffold
import com.xenikii.timecalculator.shared.components.AppTextField
import com.xenikii.timecalculator.shared.components.BackButton
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContent(
    state: AuthFormState,
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
) {
    AppScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AuthSpacing.ScreenHorizontalPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(Modifier.height(AuthSpacing.TopSpacing))
            Text(
                text = stringResource(R.string.auth_register_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(AuthSpacing.ContentSpacing))
            AppTextField(
                value = state.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.error != null,
                label = { Text(stringResource(R.string.auth_field_email)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(Modifier.height(AuthSpacing.FieldSpacing))
            AuthPasswordTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                labelResId = R.string.auth_field_password,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                isError = state.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(Modifier.height(AuthSpacing.FieldSpacing))
            AuthPasswordTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                labelResId = R.string.auth_field_confirm_password,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                isError = state.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit() }
                )
            )
            state.error?.let { error ->
                Spacer(Modifier.height(AuthSpacing.FieldSpacing))
                Text(
                    text = authFormErrorText(error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(AuthSpacing.ActionSpacing))
            AppButtonExpressive(
                onClick = onSubmit,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.auth_action_register))
                }
            }
            Spacer(Modifier.height(AuthSpacing.LinkSpacing))
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !state.isLoading,
            ) {
                Text(
                    stringResource(R.string.auth_link_to_login),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@PreviewAll
@Composable
fun RegisterContentPreview() {
    PreviewTheme {
        RegisterContent(state = AuthFormState())
    }
}
