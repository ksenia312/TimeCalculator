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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.morningcalculator.shared.extensions.toAppRoute
import com.example.morningcalculator.shared.navigator.AppNavigator
import com.example.morningcalculator.shared.navigator.AppRoute
import com.example.morningcalculator.shared.theme.MorningCalculatorTheme

class MainActivity : ComponentActivity() {

    private var startAppRoute by mutableStateOf<AppRoute?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startAppRoute = intent.toAppRoute()

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
                AppNavigator(startAppRoute = startAppRoute)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startAppRoute = intent.toAppRoute()
    }

    companion object {
        private const val PREFS_NAME = "notification_permission"
        private const val KEY_ASKED_POST_NOTIFICATIONS = "asked_post_notifications"
    }
}