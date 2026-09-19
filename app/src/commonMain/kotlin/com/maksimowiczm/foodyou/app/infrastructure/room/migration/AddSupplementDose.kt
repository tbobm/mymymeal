package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Lets a supplement optionally carry a daily dose (e.g. Creatine, grams) instead of being
 * presence-only (e.g. Ashwagandha, Multivitamin). Purely additive: `Supplement.tracksDose`
 * defaults false so every existing supplement keeps today's boolean-only behaviour, and
 * `SupplementIntake.doseGrams` is nullable and only ever populated for a dose-tracking supplement.
 */
internal val addSupplementDose =
    object : Migration(35, 36) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE Supplement ADD COLUMN tracksDose INTEGER NOT NULL DEFAULT 0"
            )
            connection.execSQL("ALTER TABLE SupplementIntake ADD COLUMN doseGrams REAL")
        }
    }
