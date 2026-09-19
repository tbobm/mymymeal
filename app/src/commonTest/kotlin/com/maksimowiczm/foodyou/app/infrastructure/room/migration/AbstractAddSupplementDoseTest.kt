package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.execSQL
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractAddSupplementDoseTest {
    abstract fun getTestHelper(): MigrationTestHelper

    open fun migrate() {
        val helper = getTestHelper()

        helper.createDatabase(35).apply {
            execSQL("INSERT INTO Supplement (id, name, sortOrder) VALUES (1, 'Vitamin D', 0)")
            execSQL("INSERT INTO SupplementIntake (supplementId, date) VALUES (1, 20000)")
            close()
        }

        val connection = helper.runMigrationsAndValidate(36, listOf(addSupplementDose))

        // Pre-existing rows survive with the new columns defaulted.
        connection.prepare("SELECT tracksDose FROM Supplement WHERE id = 1").use { statement ->
            statement.step()
            assertEquals(0, statement.getLong(0).toInt())
        }
        connection.prepare("SELECT doseGrams FROM SupplementIntake WHERE supplementId = 1").use {
            statement ->
            statement.step()
            assertNull(if (statement.isNull(0)) null else statement.getDouble(0))
        }

        // A dose-tracking supplement can record a value.
        connection.execSQL(
            "INSERT INTO Supplement (id, name, sortOrder, tracksDose) VALUES (2, 'Creatine', 1, 1)"
        )
        connection.execSQL(
            "INSERT INTO SupplementIntake (supplementId, date, doseGrams) VALUES (2, 20000, 5.0)"
        )
        connection.prepare("SELECT doseGrams FROM SupplementIntake WHERE supplementId = 2").use {
            statement ->
            statement.step()
            assertEquals(5.0, statement.getDouble(0))
        }
    }
}
