package com.xenikii.timecalculator.app.schedule

import android.content.Context
import com.xenikii.timecalculator.data.schedule.RefreshRoutineNotificationsUseCase
import com.xenikii.timecalculator.data.schedule.watchdog.RoutineScheduleWatchdogScheduler
import com.xenikii.timecalculator.domain.repository.NotificationSettingsRepository
import com.xenikii.timecalculator.domain.repository.RoutineAlarmGateway
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class RoutineScheduleInitializer(
    private val context: Context,
    private val routineRepository: RoutineRepository,
    private val scheduleRepository: RoutineScheduleRepository,
    private val alarmGateway: RoutineAlarmGateway,
    private val permissionRequester: RoutineExactAlarmPermissionRequester,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val refreshRoutineNotifications: RefreshRoutineNotificationsUseCase,
) {
    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(FlowPreview::class)
    fun start() {
        if (!started.compareAndSet(false, true)) return
        RoutineScheduleWatchdogScheduler.ensureScheduled(context)
        scope.launch {
            routineRepository.routinesFlow
                .debounce(300.milliseconds)
                .collect { routines ->
                    if (!alarmGateway.canScheduleExactAlarms() && routines.any { it.data.isNotEmpty() }) {
                        permissionRequester.promptIfNeeded()
                    }
                    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                    scheduleRepository.reconcile(routines = routines, now = now)
                    // reconcile() no-ops whenever a routine's schedule signature hasn't changed -
                    // the common case on a cold app start, since today's alarms were already
                    // correctly armed ahead of time. That means it alone can't repaint a progress
                    // notification that a killed/missed alarm failed to post. Unconditionally
                    // resync every time the app starts (or routines change) instead of only doing
                    // so when the user happens to toggle the notification setting.
                    scheduleRepository.refreshNotifications(routines = routines, now = now)
                }
        }
        scope.launch {
            notificationSettingsRepository.observeEnabled()
                .drop(1)
                .collect {
                    refreshRoutineNotifications()
                }
        }
    }
}
