package com.example.morningcalculator.data.repository

import android.content.Context
import com.example.morningcalculator.app.notifications.RoutineNotificationPublisher
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.data.db.RoutinesDao
import com.example.morningcalculator.data.mapper.toDomain
import com.example.morningcalculator.data.model.RoutineEntity
import com.example.morningcalculator.data.model.RoutineItemEntity
import com.example.morningcalculator.shared.extensions.getCurrentTaskIndex
import com.example.morningcalculator.shared.extensions.withZeroSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RoutineRepositoryImpl(
    private val dao: RoutinesDao,
    private val context: Context,
) : RoutineRepository {

    private val populatedFlow = dao.getRoutinesPopulated()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _selectedRoutineId = MutableStateFlow<String?>(null)

    override val routinesFlow: StateFlow<List<Routine>> = populatedFlow
        .map { list -> list.map { it.toDomain() } }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val notificationChecker by lazy {
        scope.launch {
            routinesFlow.collect { routines ->
                routines.forEach { routine ->
                    showNotificationIfActive(routine)
                }
            }
        }

        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(15.seconds)
                val routines = routinesFlow.value
                routines.forEach { routine ->
                    showNotificationIfActive(routine)
                }
            }
        }
    }

    init {
        notificationChecker
    }

    override val routineFlow: StateFlow<Routine?> =
        combine(routinesFlow, _selectedRoutineId) { routines, selectedId ->
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

        val routine = dao.getRoutinePopulated(routineEntity.id)
        if (routine != null) {
            val domainRoutine = routine.toDomain()
            showNotificationIfActive(domainRoutine)
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

        showNotificationIfActive(normalized)
    }

    override suspend fun deleteRoutine(id: String) {
        RoutineNotificationPublisher.dismiss(context, id)

        withContext(Dispatchers.IO) {
            dao.deleteRoutine(id)
        }
    }

    private fun showNotificationIfActive(routine: Routine) {
        if (routine.data.isEmpty()) return

        val currentTaskIndex = routine.getCurrentTaskIndex()
        if (currentTaskIndex != null) {
            RoutineNotificationPublisher.showTask(
                context = context,
                routine = routine,
                taskIndex = currentTaskIndex
            )
        } else {
            RoutineNotificationPublisher.dismiss(context, routine.id)
        }
    }
}