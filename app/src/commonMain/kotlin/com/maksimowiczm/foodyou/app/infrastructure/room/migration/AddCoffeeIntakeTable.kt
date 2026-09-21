package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Coffee tracking (habits, not food). Each tap logs a discrete, non-food event with a hardcoded
 * caffeine mg for the tapped type (`CoffeeType`) -- it is never a diary `Measurement`, so it
 * carries its own timestamp/date columns rather than reusing `Measurement`'s. Replaces the earlier
 * "default coffee" mechanism (migration 34->35's comment), which resolved cups and caffeine from a
 * real logged food; that mechanism is removed in this same change.
 */
internal val addCoffeeIntakeTable =
    object : Migration(36, 37) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `CoffeeIntake` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`dateEpochDay` INTEGER NOT NULL, " +
                    "`createdEpochSeconds` INTEGER NOT NULL, " +
                    "`type` TEXT NOT NULL, " +
                    "`caffeineMg` INTEGER NOT NULL)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_CoffeeIntake_dateEpochDay` " +
                    "ON `CoffeeIntake` (`dateEpochDay`)"
            )
        }
    }
