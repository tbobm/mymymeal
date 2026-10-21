package dev.tbobm.mymymeal.app.app.infrastructure.room.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.execSQL
import kotlin.test.assertEquals

abstract class AbstractAddUsageLogTablesTest {
    abstract fun getTestHelper(): MigrationTestHelper

    open fun migrate() {
        val helper = getTestHelper()

        helper.createDatabase(37).apply {
            execSQL("INSERT INTO Supplement (id, name, sortOrder) VALUES (1, 'Vitamin D', 0)")
            close()
        }

        val connection = helper.runMigrationsAndValidate(38, listOf(addUsageLogTables))

        connection.prepare("SELECT COUNT(*) FROM Supplement").use { statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }

        connection.execSQL("INSERT INTO UsageEvent (ts, code, arg) VALUES (1700000000, 1, NULL)")
        connection.execSQL("INSERT INTO UsageDaily (day, code, count) VALUES (19675, 1, 4)")

        connection.prepare("SELECT code, arg FROM UsageEvent").use { statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
            assertEquals(true, statement.isNull(1))
        }
        connection.prepare("SELECT count FROM UsageDaily WHERE day = 19675").use { statement ->
            statement.step()
            assertEquals(4, statement.getLong(0).toInt())
        }
    }
}
