package com.xenikii.timecalculator.shared.utils

import androidx.compose.ui.graphics.Color
import com.xenikii.timecalculator.shared.theme.Blue1
import com.xenikii.timecalculator.shared.theme.Blue2
import com.xenikii.timecalculator.shared.theme.Blue3
import com.xenikii.timecalculator.shared.theme.Green1
import com.xenikii.timecalculator.shared.theme.Green2
import com.xenikii.timecalculator.shared.theme.Pink1
import com.xenikii.timecalculator.shared.theme.Pink2
import com.xenikii.timecalculator.shared.theme.Pink3

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