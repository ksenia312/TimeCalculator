package com.xenikii.timecalculator.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Update
import com.xenikii.timecalculator.data.model.PendingDeletionEntity
import com.xenikii.timecalculator.data.model.RoutineEntity
import com.xenikii.timecalculator.data.model.RoutineItemEntity
import com.xenikii.timecalculator.data.model.SubDataEntity
import com.xenikii.timecalculator.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {
    @Transaction
    @Query("SELECT * FROM tasks")
    fun getTasks(): Flow<List<TaskWithSubData>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE pendingSync = 1")
    suspend fun getPendingTasks(): List<TaskWithSubData>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubData(subData: List<SubDataEntity>)

    @Update
    suspend fun updateSubData(subData: List<SubDataEntity>)

    @Query("SELECT * FROM sub_data WHERE taskId = :taskId")
    suspend fun getSubDataForTask(taskId: String): List<SubDataEntity>

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

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("UPDATE tasks SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun clearTasksPending(ids: List<String>)

    @Transaction
    suspend fun applyRemoteTask(task: TaskEntity, subData: List<SubDataEntity>) {
        upsertTask(task)

        if (subData.isEmpty()) {
            clearSubDataForTask(task.id)
        } else {
            insertSubData(subData)
            updateSubData(subData)
            deleteSubDataNotIn(task.id, subData.map { it.id })
        }

        nullifyInvalidRoutineItemSubDataForTask(task.id)
    }

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()
}

@Dao
interface RoutinesDao {
    @Transaction
    @Query(
        """
        SELECT r.*
        FROM routines r
        LEFT JOIN routine_items ri ON ri.routineId = r.id
        LEFT JOIN tasks t ON t.id = ri.taskId
        LEFT JOIN sub_data sd ON sd.id = ri.subDataId
        GROUP BY r.id
        """
    )
    fun getRoutinesPopulated(): Flow<List<RoutinePopulated>>

    @Transaction
    @Query(
        """
        SELECT r.*
        FROM routines r
        LEFT JOIN routine_items ri ON ri.routineId = r.id
        LEFT JOIN tasks t ON t.id = ri.taskId
        LEFT JOIN sub_data sd ON sd.id = ri.subDataId
        GROUP BY r.id
        """
    )
    suspend fun getRoutinesPopulatedOnce(): List<RoutinePopulated>

    @Transaction
    @Query(
        """
        SELECT r.*
        FROM routines r
        LEFT JOIN routine_items ri ON ri.routineId = r.id
        LEFT JOIN tasks t ON t.id = ri.taskId
        LEFT JOIN sub_data sd ON sd.id = ri.subDataId
        WHERE r.pendingSync = 1
        GROUP BY r.id
        """
    )
    suspend fun getPendingRoutines(): List<RoutinePopulated>

    @Transaction
    @Query(
        """
        SELECT r.*
        FROM routines r
        LEFT JOIN routine_items ri ON ri.routineId = r.id
        LEFT JOIN tasks t ON t.id = ri.taskId
        LEFT JOIN sub_data sd ON sd.id = ri.subDataId
        WHERE r.id = :id
        GROUP BY r.id
        """
    )
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

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: String): RoutineEntity?

    @Query("UPDATE routines SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun clearRoutinesPending(ids: List<String>)

    @Query("DELETE FROM routines")
    suspend fun clearRoutines()
}

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPendingDeletion(row: PendingDeletionEntity)

    @Query("SELECT * FROM pending_deletions")
    suspend fun getPendingDeletions(): List<PendingDeletionEntity>

    @Query("DELETE FROM pending_deletions WHERE entityType = :type AND id IN (:ids)")
    suspend fun clearPendingDeletions(type: String, ids: List<String>)

    @Query("DELETE FROM pending_deletions")
    suspend fun clearAllPendingDeletions()
}

@Database(
    entities = [
        TaskEntity::class,
        SubDataEntity::class,
        RoutineEntity::class,
        RoutineItemEntity::class,
        PendingDeletionEntity::class,
    ],
    version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tasksDao(): TasksDao

    abstract fun routinesDao(): RoutinesDao

    abstract fun syncDao(): SyncDao
}