package com.xenikii.timecalculator.data.repository

import androidx.room.withTransaction
import com.xenikii.timecalculator.data.db.AppDatabase
import com.xenikii.timecalculator.data.db.RoutinesDao
import com.xenikii.timecalculator.data.db.SyncDao
import com.xenikii.timecalculator.data.mapper.encodeRecurrenceDaysOfWeek
import com.xenikii.timecalculator.data.mapper.toDomain
import com.xenikii.timecalculator.data.model.PendingDeletionEntity
import com.xenikii.timecalculator.data.model.RoutineEntity
import com.xenikii.timecalculator.data.model.RoutineItemEntity
import com.xenikii.timecalculator.data.sync.SyncTrigger
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineRequest
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.shared.extensions.withZeroSeconds
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
    private val appDatabase: AppDatabase,
    private val routinesDao: RoutinesDao,
    private val syncDao: SyncDao,
    private val syncTrigger: SyncTrigger,
) : RoutineRepository {

    private val populatedFlow = routinesDao.getRoutinesPopulated()
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

    override suspend fun getRoutines(): List<Routine> = withContext(Dispatchers.IO) {
        routinesDao.getRoutinesPopulatedOnce().map { it.toDomain() }
    }

    override suspend fun addRoutine(request: RoutineRequest): String {
        val scheduledAt = request.scheduledAt.withZeroSeconds()
        val routineEntity = RoutineEntity(
            id = UUID.randomUUID().toString(),
            title = request.title,
            color = request.color,
            scheduledAtMillis = scheduledAt.toEpochMilliseconds(),
            scheduledAtAnchor = request.scheduledAtAnchor.name,
            recurrenceUnit = request.recurrence.unit.name,
            recurrenceInterval = request.recurrence.interval.coerceAtLeast(1),
            recurrenceDaysOfWeek = request.recurrence.daysOfWeek.encodeRecurrenceDaysOfWeek(),
            modifiedAt = System.currentTimeMillis()
        )

        withContext(Dispatchers.IO) {
            routinesDao.insertRoutine(routineEntity)
            syncTrigger.emit()
        }
        return routineEntity.id
    }

    override suspend fun updateRoutine(routine: Routine) {
        val normalized = routine.copy(scheduledAt = routine.scheduledAt.withZeroSeconds())

        val routineEntity = RoutineEntity(
            id = normalized.id,
            title = normalized.title,
            color = normalized.color,
            scheduledAtMillis = normalized.scheduledAt.toEpochMilliseconds(),
            scheduledAtAnchor = normalized.scheduledAtAnchor.name,
            recurrenceUnit = normalized.recurrence.unit.name,
            recurrenceInterval = normalized.recurrence.interval.coerceAtLeast(1),
            recurrenceDaysOfWeek = normalized.recurrence.daysOfWeek.encodeRecurrenceDaysOfWeek(),
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
            routinesDao.updateRoutineWithItems(routineEntity, itemsEntities)
            syncTrigger.emit()
        }
    }

    override suspend fun deleteRoutine(id: String) {
        withContext(Dispatchers.IO) {
            appDatabase.withTransaction {
                syncDao.addPendingDeletion(
                    PendingDeletionEntity(
                        entityType = TYPE_ROUTINE,
                        id = id,
                        modifiedAt = System.currentTimeMillis(),
                    )
                )
                routinesDao.deleteRoutine(id)
            }
            syncTrigger.emit()
        }
    }

    private companion object {
        const val TYPE_ROUTINE = "routine"
    }
}