package com.example.morningcalculator.data.db

import androidx.room.*
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.Task
import com.example.morningcalculator.data.converters.Converters
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {
    @Query("SELECT * FROM tasks")
    fun getTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()
}

@Dao
interface RoutinesDao {
    @Query("SELECT * FROM routines")
    fun getRoutines(): Flow<List<Routine.Links>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine.Links)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: String)
}

@Database(entities = [Task::class, Routine.Links::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tasksDao(): TasksDao
    abstract fun routinesDao(): RoutinesDao
}