package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.execSQL
import kotlin.test.assertEquals

abstract class AbstractAddCoffeeIntakeTableTest {
    abstract fun getTestHelper(): MigrationTestHelper

    open fun migrate() {
        val helper = getTestHelper()

        helper.createDatabase(36).apply {
            execSQL("INSERT INTO Supplement (id, name, sortOrder) VALUES (1, 'Vitamin D', 0)")
            close()
        }

        val connection = helper.runMigrationsAndValidate(37, listOf(addCoffeeIntakeTable))

        // Pre-existing rows survive untouched.
        connection.prepare("SELECT COUNT(*) FROM Supplement").use { statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }

        // The new table exists, is empty, and accepts inserts.
        connection.prepare("SELECT COUNT(*) FROM CoffeeIntake").use { statement ->
            statement.step()
            assertEquals(0, statement.getLong(0).toInt())
        }

        connection.execSQL(
            "INSERT INTO CoffeeIntake (dateEpochDay, createdEpochSeconds, type, caffeineMg) " +
                "VALUES (20000, 1700000000, 'Espresso', 63)"
        )

        connection.prepare("SELECT caffeineMg FROM CoffeeIntake WHERE dateEpochDay = 20000").use {
            statement ->
            statement.step()
            assertEquals(63, statement.getLong(0).toInt())
        }
    }
}
