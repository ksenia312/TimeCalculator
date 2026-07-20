package com.example.morningcalculator.data.repository

import com.example.morningcalculator.domain.model.Routine
import com.example.morningcalculator.domain.model.RoutineRequest
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.data.db.RoutinesDao
import com.example.morningcalculator.data.mapper.toDomain
import com.example.morningcalculator.data.model.RoutineEntity
import com.example.morningcalculator.data.model.RoutineItemEntity
import com.example.morningcalculator.shared.extensions.withZeroSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import java.util.UUID

class RoutineRepositoryImpl(
    private val dao: RoutinesDao,
) : RoutineRepository {

    private val populatedFlow = dao.getRoutinesPopulated()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val routinesFlow: Flow<List<Routine>> = populatedFlow
        .map { list -> list.map { it.toDomain() } }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    override fun getRoutineFlow(id: String): Flow<Routine?> =
        routinesFlow.map { routines -> routines.firstOrNull { it.id == id } }

    override suspend fun addRoutine(request: RoutineRequest) {
        val scheduledAt = request.scheduledAt.withZeroSeconds()

        val routineEntity = RoutineEntity(
            id = UUID.randomUUID().toString(),
            title = request.title,
            color = request.color,
            scheduledAtMillis = scheduledAt.toEpochMilliseconds(),
            scheduledAtAnchor = request.scheduledAtAnchor.name,
            modifiedAt = System.currentTimeMillis()
        )

        withContext(Dispatchers.IO) {
            dao.insertRoutine(routineEntity)
        }
    }

    override suspend fun updateRoutine(routine: Routine) {
        val normalized = routine.copy(scheduledAt = routine.scheduledAt.withZeroSeconds())

        val routineEntity = RoutineEntity(
            id = normalized.id,
            title = normalized.title,
            color = normalized.color,
            scheduledAtMillis = normalized.scheduledAt.toEpochMilliseconds(),
            scheduledAtAnchor = normalized.scheduledAtAnchor.name,
            modifiedAt = System.currentTimeMillis()
        )

        val itemsEntities = normalized.data.mapIndexed { index, link ->
            RoutineItemEntity(
                id = link.id,
                routineId = normalized.id,
                taskId = link.task.id,
                subDataId = link.subData?.id,
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
}