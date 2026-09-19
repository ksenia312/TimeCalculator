package com.xenikii.timecalculator.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.xenikii.timecalculator.data.model.RoutineEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutinesDaoLastTriggeredAtTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: RoutinesDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.routinesDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun bumpLastTriggeredAt_setsValue_andFlagsPendingSync_withoutTouchingModifiedAt() = runBlocking {
        dao.insertRoutine(routine(id = "r1", modifiedAt = 123L, pendingSync = false))

        dao.bumpLastTriggeredAt("r1", 500L)

        val row = dao.getRoutineById("r1")!!
        assertEquals(500L, row.lastTriggeredAt)
        assertEquals(123L, row.modifiedAt)
        assertEquals(true, row.pendingSync)
    }

    @Test
    fun bumpLastTriggeredAt_neverRegressesAnAlreadyNewerValue() = runBlocking {
        dao.insertRoutine(routine(id = "r1", lastTriggeredAt = 1_000L))

        dao.bumpLastTriggeredAt("r1", 1L)

        assertEquals(1_000L, dao.getRoutineById("r1")!!.lastTriggeredAt)
    }

    @Test
    fun bumpLastTriggeredAt_fromNull_setsTheValue() = runBlocking {
        dao.insertRoutine(routine(id = "r1", lastTriggeredAt = null))

        dao.bumpLastTriggeredAt("r1", 42L)

        assertEquals(42L, dao.getRoutineById("r1")!!.lastTriggeredAt)
    }

    private fun routine(
        id: String,
        modifiedAt: Long = 0L,
        pendingSync: Boolean = true,
        lastTriggeredAt: Long? = null,
    ) = RoutineEntity(
        id = id,
        title = "Routine",
        color = "#000000",
        scheduledAtMillis = 0L,
        scheduledAtAnchor = "START",
        modifiedAt = modifiedAt,
        pendingSync = pendingSync,
        lastTriggeredAt = lastTriggeredAt,
    )
}
