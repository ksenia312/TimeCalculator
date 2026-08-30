package com.xenikii.timecalculator.data.schedule

import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import kotlin.time.Instant

class RefreshRoutineNotificationsUseCase(
    private val routineRepository: RoutineRepository,
    private val scheduleRepository: RoutineScheduleRepository,
) {
    suspend operator fun invoke() {
        val routines = routineRepository.getRoutines()
        scheduleRepository.refreshNotifications(
            routines = routines,
            now = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
        )
    }
}
