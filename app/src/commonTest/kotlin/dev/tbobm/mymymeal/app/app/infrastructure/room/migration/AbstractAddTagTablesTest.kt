package dev.tbobm.mymymeal.app.app.infrastructure.room.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.execSQL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AbstractAddTagTablesTest {
    abstract fun getTestHelper(): MigrationTestHelper

    open fun migrate() {
        val helper = getTestHelper()

        helper.createDatabase(33).apply {
            execSQL(
                "INSERT INTO Meal (id, name, fromHour, fromMinute, toHour, toMinute, rank) " +
                    "VALUES (1, 'Breakfast', 6, 0, 10, 0, 0)"
            )
            execSQL(
                "INSERT INTO Product (id, name, sourceType, isLiquid) " +
                    "VALUES (1, 'Test Product', 0, 0)"
            )
            execSQL(
                "INSERT INTO Recipe (id, name, servings, isLiquid) VALUES (1, 'Test Recipe', 2, 0)"
            )
            execSQL(
                "INSERT INTO ManualDiaryEntry " +
                    "(id, mealId, dateEpochDay, name, createdEpochSeconds, updatedEpochSeconds) " +
                    "VALUES (1, 1, 100, 'Manual entry', 1000, 1000)"
            )
            close()
        }

        val connection = helper.runMigrationsAndValidate(34, listOf(addTagTables))

        // Pre-existing rows survive the migration untouched.
        connection.prepare("SELECT COUNT(*) FROM Product").use { statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }
        connection.prepare("SELECT COUNT(*) FROM Recipe").use { statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }
        connection.prepare("SELECT COUNT(*) FROM ManualDiaryEntry").use { statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }

        // The new tables exist, are empty, and accept inserts + cascade deletes correctly.
        connection.prepare("SELECT COUNT(*) FROM Tag").use { statement ->
            statement.step()
            assertEquals(0, statement.getLong(0).toInt())
        }

        connection.execSQL("INSERT INTO Tag (id, name) VALUES (1, 'Vegetarian')")
        connection.execSQL("INSERT INTO ProductTagCrossRef (productId, tagId) VALUES (1, 1)")
        connection.execSQL("INSERT INTO RecipeTagCrossRef (recipeId, tagId) VALUES (1, 1)")
        connection.execSQL(
            "INSERT INTO ManualDiaryEntryTagCrossRef (manualDiaryEntryId, tagId) VALUES (1, 1)"
        )

        connection.prepare("SELECT COUNT(*) FROM ProductTagCrossRef WHERE tagId = 1").use {
            statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }

        // Deleting the tag cascades into every cross-ref table (onDelete = CASCADE).
        connection.execSQL("DELETE FROM Tag WHERE id = 1")

        connection.prepare("SELECT COUNT(*) FROM ProductTagCrossRef").use { statement ->
            statement.step()
            assertTrue(statement.getLong(0) == 0L)
        }
        connection.prepare("SELECT COUNT(*) FROM RecipeTagCrossRef").use { statement ->
            statement.step()
            assertTrue(statement.getLong(0) == 0L)
        }
        connection.prepare("SELECT COUNT(*) FROM ManualDiaryEntryTagCrossRef").use { statement ->
            statement.step()
            assertTrue(statement.getLong(0) == 0L)
        }
    }
}
