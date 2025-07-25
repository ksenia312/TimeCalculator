package com.example.morningcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.morningcalculator.features.home.ui.HomeScreen
import com.example.morningcalculator.shared.navigator.AppNavigator
import com.example.morningcalculator.shared.theme.MorningCalculatorTheme
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MorningCalculatorTheme {
                AppNavigator()
            }
        }
    }
}

