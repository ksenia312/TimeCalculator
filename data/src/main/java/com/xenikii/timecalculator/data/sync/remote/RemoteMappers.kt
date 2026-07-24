package com.xenikii.timecalculator.data.sync.remote

import com.xenikii.timecalculator.data.db.RoutinePopulated
import com.xenikii.timecalculator.data.db.TaskWithSubData
import com.xenikii.timecalculator.data.model.RoutineEntity
import com.xenikii.timecalculator.data.model.RoutineItemEntity
import com.xenikii.timecalculator.data.model.SubDataEntity
import com.xenikii.timecalculator.data.model.TaskEntity
import kotlin.time.Duration

fun TaskWithSubData.toRemote(): RemoteTask =
    RemoteTask(
        id = task.id,
        title = task.title,
        description = task.description,
        subData = subDataList.map { RemoteSubData(id = it.id, duration = it.duration.toString()) },
        modifiedAt = task.modifiedAt ?: 0L,
        deleted = false,
    )

fun RemoteTask.toEntities(): Pair<TaskEntity, List<SubDataEntity>> =
    TaskEntity(
        id = id,
        title = title,
        description = description,
        modifiedAt = modifiedAt,
        pendingSync = false,
    ) to subData.map { remoteSubData ->
        SubDataEntity(
            id = remoteSubData.id,
            taskId = id,
            duration = Duration.parse(remoteSubData.duration),
        )
    }

fun RoutinePopulated.toRemote(): RemoteRoutine =
    RemoteRoutine(
        id = routine.id,
        title = routine.title,
        color = routine.color,
        scheduledAtMillis = routine.scheduledAtMillis,
        scheduledAtAnchor = routine.scheduledAtAnchor,
        items = items.sortedBy { it.item.orderIndex }.map { item ->
            RemoteRoutineItem(
                id = item.item.id,
                taskId = item.item.taskId,
                subDataId = item.item.subDataId,
                orderIndex = item.item.orderIndex,
            )
        },
        modifiedAt = routine.modifiedAt,
        deleted = false,
    )

fun RemoteRoutine.toEntities(): Pair<RoutineEntity, List<RoutineItemEntity>> =
    RoutineEntity(
        id = id,
        title = title,
        color = color,
        scheduledAtMillis = scheduledAtMillis,
        scheduledAtAnchor = scheduledAtAnchor,
        modifiedAt = modifiedAt,
        pendingSync = false,
    ) to items.map { item ->
        RoutineItemEntity(
            id = item.id,
            routineId = id,
            taskId = item.taskId,
            subDataId = item.subDataId,
            orderIndex = item.orderIndex,
        )
    }

fun tombstoneTask(id: String, modifiedAt: Long): RemoteTask =
    RemoteTask(
        id = id,
        title = "",
        description = "",
        subData = emptyList(),
        modifiedAt = modifiedAt,
        deleted = true,
    )

fun tombstoneRoutine(id: String, modifiedAt: Long): RemoteRoutine =
    RemoteRoutine(
        id = id,
        title = "",
        color = "",
        scheduledAtMillis = 0L,
        scheduledAtAnchor = "START",
        items = emptyList(),
        modifiedAt = modifiedAt,
        deleted = true,
    )
