package com.example.morningcalculator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.morningcalculator.shared.navigator.AppNavigator
import com.example.morningcalculator.shared.theme.MorningCalculatorTheme

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