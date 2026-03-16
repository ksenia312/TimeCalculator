package com.example.morningcalculator.data.repository

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineLink
import com.example.morningcalculator.core.model.RoutineRequest
import com.example.morningcalculator.core.model.SubData
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.data.db.RoutinePopulated
import com.example.morningcalculator.data.db.RoutinesDao
import com.example.morningcalculator.data.model.RoutineEntity
import com.example.morningcalculator.data.model.RoutineItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.util.UUID

class RoutineRepositoryImpl(
    private val dao: RoutinesDao
) : RoutineRepository {

    private val populatedFlow = dao.getRoutinesPopulated()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _selectedRoutineId = MutableStateFlow<String?>(null)

    override val routinesFlow: StateFlow<List<Routine>> = populatedFlow
        .map { list -> list.map { populated -> mapToDomain(populated) } }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override val routineFlow: StateFlow<Routine?> = combine(
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

    override suspend fun addRoutine(request: RoutineRequest) {
        val routineEntity = RoutineEntity(
            id = UUID.randomUUID().toString(),
            title = request.title,
            color = request.color,
            time = request.time,
            modifiedAt = System.currentTimeMillis()
        )

        withContext(Dispatchers.IO) {
            dao.insertRoutine(routineEntity)
        }
    }

    override suspend fun updateRoutine(routine: Routine) {
        val routineEntity = RoutineEntity(
            id = routine.id,
            title = routine.title,
            color = routine.color,
            time = routine.time,
            modifiedAt = System.currentTimeMillis()
        )

        val itemsEntities = routine.data.mapIndexed { index, link ->
            RoutineItemEntity(
                id = link.id,
                routineId = routine.id,
                taskId = link.task.id,
                subDataId = link.subData.id,
                orderIndex = index
            )
        }

        withContext(Dispatchers.IO) {
            dao.updateRoutineWithItems(routineEntity, itemsEntities)
        }
    }

    override suspend fun deleteRoutine(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteRoutine(id)
        }
    }

    private fun mapToDomain(populated: RoutinePopulated): Routine {
        val sortedItems = populated.items.sortedBy { it.item.orderIndex }

        val fullLinks = sortedItems.map { itemPopulated ->
            val taskWithData = itemPopulated.taskWithData
            val taskEntity = taskWithData.task
            val allSubDataEntities = taskWithData.subDataList

            RoutineLink(
                id = itemPopulated.item.id,
                task = Task(
                    id = taskEntity.id,
                    title = taskEntity.title,
                    description = taskEntity.description,
                    data = allSubDataEntities.map { SubData(it.id, it.duration) },
                    modifiedAt = taskEntity.modifiedAt
                ),
                subData = SubData(
                    id = itemPopulated.subData.id,
                    duration = itemPopulated.subData.duration
                )
            )
        }

        return Routine(
            id = populated.routine.id,
            title = populated.routine.title,
            color = populated.routine.color,
            time = populated.routine.time,
            modifiedAt = populated.routine.modifiedAt,
            data = fullLinks
        )
    }
}