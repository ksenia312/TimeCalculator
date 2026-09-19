package com.xenikii.timecalculator.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE routines ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 1")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pending_deletions (
                entityType TEXT NOT NULL,
                id TEXT NOT NULL,
                modifiedAt INTEGER NOT NULL,
                PRIMARY KEY(entityType, id)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE routines ADD COLUMN recurrenceUnit TEXT NOT NULL DEFAULT 'NONE'")
        db.execSQL("ALTER TABLE routines ADD COLUMN recurrenceInterval INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE routines ADD COLUMN recurrenceDaysOfWeek TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE routines ADD COLUMN pauseState TEXT NOT NULL DEFAULT 'ACTIVE'")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE routines ADD COLUMN lastTriggeredAt INTEGER")
    }
}
