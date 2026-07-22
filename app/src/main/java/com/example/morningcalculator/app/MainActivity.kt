package com.example.morningcalculator.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.navigation3.runtime.NavKey
import com.example.morningcalculator.shared.extensions.toAppRoute
import com.example.morningcalculator.shared.navigator.AppNavigator
import com.example.morningcalculator.shared.navigator.AppRoute
import com.example.morningcalculator.shared.navigator.PendingDeepLink
import com.example.morningcalculator.shared.theme.MorningCalculatorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Neutral start: the auth gate in AppNavigator decides the real destination once the
        // session status is known. A deep link (from a notification) is deferred and applied by
        // the gate after the user is logged in.
        intent.toAppRoute()?.let { PendingDeepLink.route.value = it }
        val initialBackStack: List<NavKey> = listOf(AppRoute.Welcome)

        setContent {
            val requestPermission = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = RequestPermission(),
            ) { _ ->
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit {
                        putBoolean(KEY_ASKED_POST_NOTIFICATIONS, true)
                    }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    val alreadyAsked = prefs.getBoolean(KEY_ASKED_POST_NOTIFICATIONS, false)
                    if (!granted && !alreadyAsked) {
                        prefs.edit { putBoolean(KEY_ASKED_POST_NOTIFICATIONS, true) }
                        requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MorningCalculatorTheme {
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
        // Defer to the gate: it opens the deep link immediately if logged in, or after login.
        PendingDeepLink.route.value = appRoute
    }

    companion object {
        private const val PREFS_NAME = "notification_permission"
        private const val KEY_ASKED_POST_NOTIFICATIONS = "asked_post_notifications"
    }
}