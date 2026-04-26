package com.example.morningcalculator.app.bootstrap

import com.example.morningcalculator.data.db.RoutinesDao
import com.example.morningcalculator.data.manager.RoutineAlarmSchedulerManager
import com.example.morningcalculator.data.mapper.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RoutineAlarmsBootstrapper(
    private val routinesDao: RoutinesDao,
    private val scheduler: RoutineAlarmSchedulerManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            routinesDao.getRoutinesPopulated()
                .map { list -> list.map { it.toDomain() } }
                .collect { routines ->
                    scheduler.rescheduleAll(routines)
                }
        }
    }
}