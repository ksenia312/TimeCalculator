package com.xenikii.timecalculator.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate4To5_defaultsExistingRoutinesToActive() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """
                INSERT INTO routines (
                    id, title, color, scheduledAtMillis, scheduledAtAnchor,
                    recurrenceUnit, recurrenceInterval, recurrenceDaysOfWeek, modifiedAt, pendingSync
                ) VALUES (
                    'routine-1', 'Morning', '#000000', 0, 'START', 'NONE', 1, '', 0, 1
                )
                """.trimIndent()
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

        migrated.query("SELECT pauseState FROM routines WHERE id = 'routine-1'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("ACTIVE", cursor.getString(0))
        }
    }

    @Test
    fun migrate5To6_addsNullableLastTriggeredAtColumn() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """
                INSERT INTO routines (
                    id, title, color, scheduledAtMillis, scheduledAtAnchor,
                    recurrenceUnit, recurrenceInterval, recurrenceDaysOfWeek, modifiedAt,
                    pendingSync, pauseState
                ) VALUES (
                    'routine-1', 'Morning', '#000000', 0, 'START', 'NONE', 1, '', 0, 1, 'ACTIVE'
                )
                """.trimIndent()
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

        migrated.query("SELECT lastTriggeredAt FROM routines WHERE id = 'routine-1'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(true, cursor.isNull(0))
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
