package com.example.morningcalculator.data.db

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
