package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Habits tracking (coffee + supplements). Adds `Supplement` (the list of supplements the user
 * takes) and `SupplementIntake` (adherence -- presence of a row means taken that day). Purely
 * additive, no existing table or column is altered. Coffee tracking needs no new table -- it
 * reuses `MeasurementSuggestion` (cup count) and the existing per-day caffeine aggregate.
 */
internal val addHabitsTables =
    object : Migration(34, 35) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `Supplement` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`sortOrder` INTEGER NOT NULL)"
            )

            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `SupplementIntake` (" +
                    "`supplementId` INTEGER NOT NULL, " +
                    "`date` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`supplementId`, `date`), " +
                    "FOREIGN KEY(`supplementId`) REFERENCES `Supplement`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_SupplementIntake_supplementId` " +
                    "ON `SupplementIntake` (`supplementId`)"
            )
        }
    }
