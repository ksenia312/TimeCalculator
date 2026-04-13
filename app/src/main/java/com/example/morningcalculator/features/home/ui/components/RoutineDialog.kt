package com.example.morningcalculator.features.home.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import com.example.morningcalculator.core.model.RoutineScheduleAnchor
import com.example.morningcalculator.shared.components.AppTextField
import com.example.morningcalculator.shared.components.DatePickerField
import com.example.morningcalculator.shared.components.TimePickerField
import com.example.morningcalculator.shared.extensions.toHexString
import com.example.morningcalculator.shared.utils.RoutineColorPicker
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDialog(
    onConfirm: (RoutineRequest) -> Unit,
    onDismiss: () -> Unit,
    initialRoutine: Routine? = null
) {
    val zoneId = ZoneId.systemDefault()

    val initialMillis = initialRoutine?.scheduledAt?.toEpochMilliseconds()
    val initialDateTime = initialMillis?.let {
        java.time.Instant.ofEpochMilli(it).atZone(zoneId).toLocalDateTime()
    }

    var title by remember { mutableStateOf(initialRoutine?.title ?: "") }
    var anchor by remember {
        mutableStateOf(
            initialRoutine?.scheduledAtAnchor ?: RoutineScheduleAnchor.END
        )
    }
    var date by remember {
        mutableStateOf(
            initialDateTime?.toLocalDate() ?: LocalDate.now(zoneId).plusDays(1)
        )
    }
    var time by remember {
        mutableStateOf(
            initialDateTime?.toLocalTime()?.let {
                LocalTime(it.hour, it.minute, it.second, it.nano)
            } ?: LocalTime(7, 0)
        )
    }

    val options = remember {
        listOf(
            RoutineScheduleAnchor.START to "Start at",
            RoutineScheduleAnchor.END to "End at"
        )
    }

    AlertDialog(
        modifier = Modifier.imePadding(),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val scheduledAtMillis = LocalDateTime
                        .of(date, time.toJavaLocalTime())
                        .atZone(zoneId)
                        .toInstant()
                        .toEpochMilli()

                    onConfirm(
                        RoutineRequest(
                            title = title,
                            scheduledAt = Instant.fromEpochMilliseconds(scheduledAtMillis),
                            scheduledAtAnchor = anchor,
                            color = RoutineColorPicker.pick().toHexString()
                        )
                    )
                    onDismiss()
                }
            ) { Text("Ok") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (initialRoutine == null) "Add new routine" else "Edit routine") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AppTextField(
                    value = title,
                    autofocus = title.isEmpty(),
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") }
                )

                Spacer(Modifier.height(12.dp))

                RoutineAnchorSelector(
                    options = options,
                    anchor = anchor,
                    onChanged = { anchor = it }
                )

                Spacer(Modifier.height(12.dp))

                DatePickerField(
                    label = "Date",
                    initialDate = date,
                    onDateChange = { date = it }
                )

                Spacer(Modifier.height(12.dp))

                TimePickerField(
                    label = if (anchor == RoutineScheduleAnchor.START) {
                        "Start time"
                    } else {
                        "End time"
                    },
                    initialTime = time,
                    onTimeChange = { time = it }
                )
            }
        }
    )
}