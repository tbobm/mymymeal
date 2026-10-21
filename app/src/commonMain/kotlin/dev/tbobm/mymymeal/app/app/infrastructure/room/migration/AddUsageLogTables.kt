package dev.tbobm.mymymeal.app.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/** Local usage log: raw action/navigation events plus a per-day rollup. Purely additive. */
internal val addUsageLogTables =
    object : Migration(37, 38) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `UsageEvent` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`ts` INTEGER NOT NULL, " +
                    "`code` INTEGER NOT NULL, " +
                    "`arg` INTEGER)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_UsageEvent_ts` ON `UsageEvent` (`ts`)"
            )
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `UsageDaily` (" +
                    "`day` INTEGER NOT NULL, " +
                    "`code` INTEGER NOT NULL, " +
                    "`count` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`day`, `code`))"
            )
        }
    }
