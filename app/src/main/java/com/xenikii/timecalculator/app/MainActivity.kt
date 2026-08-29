package com.xenikii.timecalculator.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.NavKey
import com.xenikii.timecalculator.shared.extensions.toAppRoute
import com.xenikii.timecalculator.shared.navigator.AppNavigator
import com.xenikii.timecalculator.shared.navigator.AppRoute
import com.xenikii.timecalculator.shared.navigator.PendingDeepLink
import com.xenikii.timecalculator.shared.theme.TimeCalculatorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent.toAppRoute()?.let { PendingDeepLink.route.value = it }

        setContent {
            val initialBackStack: List<NavKey> = listOf(
                AppRoute.Welcome,
            )

            TimeCalculatorTheme {
                AppNavigator(
                    initialBackStack = initialBackStack,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val appRoute = intent.toAppRoute() ?: return
        PendingDeepLink.route.value = appRoute
    }
}