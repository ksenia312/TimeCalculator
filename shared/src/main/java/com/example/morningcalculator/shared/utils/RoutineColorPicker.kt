package com.example.morningcalculator.shared.utils

import androidx.compose.ui.graphics.Color
import com.example.morningcalculator.shared.theme.Blue1
import com.example.morningcalculator.shared.theme.Blue2
import com.example.morningcalculator.shared.theme.Blue3
import com.example.morningcalculator.shared.theme.Green1
import com.example.morningcalculator.shared.theme.Green2
import com.example.morningcalculator.shared.theme.Pink1
import com.example.morningcalculator.shared.theme.Pink2
import com.example.morningcalculator.shared.theme.Pink3

object RoutineColorPicker {
    fun pick(): Color {
        return routineColors.random()
    }

    private val routineColors = listOf(
        Pink1,
        Pink2,
        Pink3,
        Blue1,
        Blue2,
        Blue3,
        Green1,
        Green2,
    )
}