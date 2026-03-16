package com.example.morningcalculator.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubData(subData: List<SubDataEntity>)

    @Query("DELETE FROM sub_data WHERE taskId = :taskId")
    suspend fun clearSubDataForTask(taskId: String)

    @Query(
        """
        DELETE FROM sub_data
        WHERE taskId = :taskId AND id NOT IN (:keepIds)
        """
    )
    suspend fun deleteSubDataNotIn(taskId: String, keepIds: List<String>)

    @Transaction
    suspend fun insertTaskWithData(task: TaskEntity, subData: List<SubDataEntity>) {
        insertTask(task)
        if (subData.isEmpty()) {
            clearSubDataForTask(task.id)
        } else {
            insertSubData(subData)
            deleteSubDataNotIn(task.id, subData.map { it.id })
        }
    }

    @Transaction
    suspend fun updateTaskWithData(task: TaskEntity, subData: List<SubDataEntity>) {
        insertTask(task)
        if (subData.isEmpty()) {
            clearSubDataForTask(task.id)
        } else {
            insertSubData(subData)
            deleteSubDataNotIn(task.id, subData.map { it.id })
        }
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineItems(items: List<RoutineItemEntity>)

    @Query("DELETE FROM routine_items WHERE routineId = :routineId")
    suspend fun clearItemsForRoutine(routineId: String)

    @Transaction
    suspend fun updateRoutineWithItems(
        routine: RoutineEntity,
        items: List<RoutineItemEntity>
    ) {
        insertRoutine(routine)
        clearItemsForRoutine(routine.id)
        insertRoutineItems(items)
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
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tasksDao(): TasksDao
    abstract fun routinesDao(): RoutinesDao
}