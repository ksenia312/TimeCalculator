package com.example.morningcalculator.data.repository

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.data.db.RoutinesDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RoutineRepositoryImpl(
    private val dao: RoutinesDao
) : RoutineRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _selectedRoutineId = MutableStateFlow<String?>(null)

    override val routinesFlow: StateFlow<List<Routine.Links>> = dao.getRoutines()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override val routineFlow: StateFlow<Routine.Links?> = combine(
        routinesFlow,
        _selectedRoutineId
    ) { routines, selectedId ->
        routines.firstOrNull { it.id == selectedId }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    override fun initializeId(id: String) {
        _selectedRoutineId.value = id
    }

    override fun clearId() {
        _selectedRoutineId.value = null
    }

    override fun addRoutine(request: RoutineRequest) {
        val routine = Routine.Links(
            id = UUID.randomUUID().toString(),
            title = request.title,
            links = emptyList(),
            time = request.time,
            color = request.color,
            modifiedAt = System.currentTimeMillis(),
        )
        addOrChangeRoutine(routine)
    }

    override fun updateRoutine(routine: Routine.Links) {
        addOrChangeRoutine(routine)
    }

    private fun addOrChangeRoutine(initialRoutine: Routine.Links) {
        val newRoutine = initialRoutine.copy(modifiedAt = System.currentTimeMillis())
        scope.launch {
            dao.insertRoutine(newRoutine)
        }
    }
}