package com.example.morningcalculator.data.db

import androidx.room.Embedded
import androidx.room.Relation
import com.example.morningcalculator.data.model.RoutineEntity
import com.example.morningcalculator.data.model.RoutineItemEntity
import com.example.morningcalculator.data.model.SubDataEntity
import com.example.morningcalculator.data.model.TaskEntity

data class TaskWithSubData(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subDataList: List<SubDataEntity>
)

data class RoutineItemPopulated(
    @Embedded val item: RoutineItemEntity,

    @Relation(
        entity = TaskEntity::class,
        parentColumn = "taskId",
        entityColumn = "id"
    )
    val taskWithData: TaskWithSubData,

    @Relation(parentColumn = "subDataId", entityColumn = "id")
    val subData: SubDataEntity
)

data class RoutinePopulated(
    @Embedded val routine: RoutineEntity,

    @Relation(
        entity = RoutineItemEntity::class,
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val items: List<RoutineItemPopulated>
)