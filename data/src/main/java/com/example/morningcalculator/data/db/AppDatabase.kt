package com.example.morningcalculator.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.morningcalculator.data.model.RoutineEntity
import com.example.morningcalculator.data.model.RoutineItemEntity
import com.example.morningcalculator.data.model.SubDataEntity
import com.example.morningcalculator.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {
    @Transaction
    @Query("SELECT * FROM tasks")
    fun getTasks(): Flow<List<TaskWithSubData>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubData(subData: List<SubDataEntity>)

    @Update
    suspend fun updateSubData(subData: List<SubDataEntity>)

    @Query("DELETE FROM sub_data WHERE taskId = :taskId")
    suspend fun clearSubDataForTask(taskId: String)

    @Query(
        """
        DELETE FROM sub_data
        WHERE taskId = :taskId AND id NOT IN (:keepIds)
        """
    )
    suspend fun deleteSubDataNotIn(taskId: String, keepIds: List<String>)

    @Query(
        """
        UPDATE routine_items
        SET subDataId = NULL
        WHERE taskId = :taskId
          AND subDataId IS NOT NULL
          AND subDataId NOT IN (SELECT id FROM sub_data WHERE taskId = :taskId)
        """
    )
    suspend fun nullifyInvalidRoutineItemSubDataForTask(taskId: String)

    @Transaction
    suspend fun insertTaskWithData(task: TaskEntity, subData: List<SubDataEntity>) {
        insertTask(task)

        if (subData.isEmpty()) {
            clearSubDataForTask(task.id)
        } else {
            insertSubData(subData)
            updateSubData(subData)
            deleteSubDataNotIn(task.id, subData.map { it.id })
        }

        nullifyInvalidRoutineItemSubDataForTask(task.id)
    }

    @Transaction
    suspend fun updateTaskWithData(task: TaskEntity, subData: List<SubDataEntity>) {
        updateTask(task)

        if (subData.isEmpty()) {
            clearSubDataForTask(task.id)
        } else {
            insertSubData(subData)
            updateSubData(subData)
            deleteSubDataNotIn(task.id, subData.map { it.id })
        }

        nullifyInvalidRoutineItemSubDataForTask(task.id)
    }

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()
}

@Dao
interface RoutinesDao {
    @Transaction
    @Query("SELECT * FROM routines")
    fun getRoutinesPopulated(): Flow<List<RoutinePopulated>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutinePopulated(id: String): RoutinePopulated?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineItems(items: List<RoutineItemEntity>)

    @Query("DELETE FROM routine_items WHERE routineId = :routineId")
    suspend fun clearItemsForRoutine(routineId: String)

    @Query("SELECT id FROM sub_data WHERE id IN (:ids)")
    suspend fun getExistingSubDataIds(ids: List<String>): List<String>

    @Query("SELECT id FROM tasks WHERE id IN (:ids)")
    suspend fun getExistingTaskIds(ids: List<String>): List<String>

    @Transaction
    suspend fun updateRoutineWithItems(
        routine: RoutineEntity,
        items: List<RoutineItemEntity>
    ) {
        insertRoutine(routine)

        val existingTaskIds =
            getExistingTaskIds(items.map { it.taskId }.distinct()).toHashSet()

        val subDataIds = items.mapNotNull { it.subDataId }.distinct()
        val existingSubDataIds = if (subDataIds.isEmpty()) {
            emptySet()
        } else {
            getExistingSubDataIds(subDataIds).toHashSet()
        }

        val sanitizedItems = items
            .filter { existingTaskIds.contains(it.taskId) }
            .map { item ->
                val subId = item.subDataId
                if (subId == null || existingSubDataIds.contains(subId)) item
                else item.copy(subDataId = null)
            }

        clearItemsForRoutine(routine.id)
        insertRoutineItems(sanitizedItems)
    }

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: String)
}

@Database(
    entities = [
        TaskEntity::class,
        SubDataEntity::class,
        RoutineEntity::class,
        RoutineItemEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tasksDao(): TasksDao

    abstract fun routinesDao(): RoutinesDao
}