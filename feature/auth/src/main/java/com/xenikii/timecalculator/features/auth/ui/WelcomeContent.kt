package com.xenikii.timecalculator.features.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.xenikii.timecalculator.R
import com.xenikii.timecalculator.features.auth.presentation.WelcomeState
import com.xenikii.timecalculator.shared.components.AppButton
import com.xenikii.timecalculator.shared.components.AppOutlinedButton
import com.xenikii.timecalculator.shared.components.AppScaffold
import com.xenikii.timecalculator.shared.preview.PreviewAll
import com.xenikii.timecalculator.shared.preview.PreviewTheme

@Composable
fun WelcomeContent(
    state: WelcomeState,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
) {
    AppScaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        when (state) {
            WelcomeState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            WelcomeState.Content -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = AuthSpacing.ScreenHorizontalPadding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.auth_welcome_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(AuthSpacing.TitleSpacing))
                    Text(
                        text = stringResource(R.string.auth_welcome_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(AuthSpacing.ContentSpacing))
                    AppButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth(0.5f),
                    ) {
                        Text(stringResource(R.string.auth_welcome_login))
                    }
                    Spacer(Modifier.height(AuthSpacing.LinkSpacing))
                    AppOutlinedButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth(0.5f),
                    ) {
                        Text(stringResource(R.string.auth_welcome_register))
                    }
                }
            }
        }
    }
}

@PreviewAll
@Composable
fun WelcomeContentPreview() {
    PreviewTheme {
        WelcomeContent(state = WelcomeState.Content)
    }
}

@PreviewAll
@Composable
fun WelcomeContentLoadingPreview() {
    PreviewTheme {
        WelcomeContent(state = WelcomeState.Loading)
    }
}
