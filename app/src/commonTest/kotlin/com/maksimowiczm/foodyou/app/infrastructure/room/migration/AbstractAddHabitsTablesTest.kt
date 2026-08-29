package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.execSQL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AbstractAddHabitsTablesTest {
    abstract fun getTestHelper(): MigrationTestHelper

    open fun migrate() {
        val helper = getTestHelper()

        helper.createDatabase(34).apply {
            execSQL(
                "INSERT INTO Meal (id, name, fromHour, fromMinute, toHour, toMinute, rank) " +
                    "VALUES (1, 'Breakfast', 6, 0, 10, 0, 0)"
            )
            close()
        }

        val connection = helper.runMigrationsAndValidate(35, listOf(addHabitsTables))

        // Pre-existing rows survive the migration untouched.
        connection.prepare("SELECT COUNT(*) FROM Meal").use { statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }

        // The new tables exist, are empty, and accept inserts + cascade deletes correctly.
        connection.prepare("SELECT COUNT(*) FROM Supplement").use { statement ->
            statement.step()
            assertEquals(0, statement.getLong(0).toInt())
        }

        connection.execSQL("INSERT INTO Supplement (id, name, sortOrder) VALUES (1, 'Vitamin D', 0)")
        connection.execSQL("INSERT INTO SupplementIntake (supplementId, date) VALUES (1, 20000)")

        connection.prepare("SELECT COUNT(*) FROM SupplementIntake WHERE supplementId = 1").use {
            statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }

        // Deleting the supplement cascades into its intake rows (onDelete = CASCADE).
        connection.execSQL("DELETE FROM Supplement WHERE id = 1")

        connection.prepare("SELECT COUNT(*) FROM SupplementIntake").use { statement ->
            statement.step()
            assertTrue(statement.getLong(0) == 0L)
        }
    }
}
