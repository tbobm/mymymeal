package dev.tbobm.mymymeal.app.app.infrastructure.room.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.execSQL
import kotlin.test.assertTrue

abstract class AbstractAddProvenanceAndCostColumnsTest {
    abstract fun getTestHelper(): MigrationTestHelper

    open fun migrate() {
        val helper = getTestHelper()

        helper.createDatabase(32).apply {
            execSQL(
                "INSERT INTO Meal (id, name, fromHour, fromMinute, toHour, toMinute, rank) " +
                    "VALUES (1, 'Breakfast', 6, 0, 10, 0, 0)"
            )
            execSQL(
                "INSERT INTO DiaryProduct (id, name, isLiquid, sourceType) " +
                    "VALUES (1, 'Test Diary Product', 0, 0)"
            )
            execSQL(
                "INSERT INTO DiaryRecipe (id, name, servings, isLiquid) " +
                    "VALUES (1, 'Test Diary Recipe', 2, 0)"
            )
            // Product-backed entry.
            execSQL(
                "INSERT INTO Measurement " +
                    "(id, mealId, epochDay, productId, recipeId, measurement, quantity, createdAt, updatedAt) " +
                    "VALUES (1, 1, 100, 1, NULL, 0, 100.0, 1000, 1000)"
            )
            // Recipe-backed entry -- sourceKind is deterministically backfillable for this one.
            execSQL(
                "INSERT INTO Measurement " +
                    "(id, mealId, epochDay, productId, recipeId, measurement, quantity, createdAt, updatedAt) " +
                    "VALUES (2, 1, 100, NULL, 1, 0, 100.0, 1000, 1000)"
            )
            execSQL(
                "INSERT INTO ManualDiaryEntry " +
                    "(id, mealId, dateEpochDay, name, createdEpochSeconds, updatedEpochSeconds) " +
                    "VALUES (1, 1, 100, 'Manual entry', 1000, 1000)"
            )
            execSQL(
                "INSERT INTO Product (id, name, sourceType, isLiquid) " +
                    "VALUES (1, 'Test Product', 0, 0)"
            )
            close()
        }

        val connection = helper.runMigrationsAndValidate(33, listOf(addProvenanceAndCostColumns))

        connection
            .prepare(
                "SELECT sourceKind, confidence, originProductId, originRecipeId FROM Measurement WHERE id = 1"
            )
            .use { statement ->
                statement.step()
                assertTrue { statement.isNull(0) } // sourceKind: no historical record
                assertTrue { statement.isNull(1) } // confidence
                assertTrue { statement.isNull(2) } // originProductId
                assertTrue { statement.isNull(3) } // originRecipeId
            }

        connection
            .prepare("SELECT sourceKind, confidence FROM Measurement WHERE id = 2")
            .use { statement ->
                statement.step()
                assertTrue { statement.getText(0) == "recipe" } // deterministic backfill
                assertTrue { statement.isNull(1) } // confidence stays unknown
            }

        connection
            .prepare("SELECT sourceKind, confidence, unitCost, currency FROM ManualDiaryEntry WHERE id = 1")
            .use { statement ->
                statement.step()
                assertTrue { statement.getText(0) == "manual_estimate" }
                assertTrue { statement.getText(1) == "estimated" }
                assertTrue { statement.isNull(2) } // unitCost
                assertTrue { statement.isNull(3) } // currency
            }

        connection.prepare("SELECT pricePerUnit, currency FROM Product WHERE id = 1").use {
            statement ->
            statement.step()
            assertTrue { statement.isNull(0) }
            assertTrue { statement.isNull(1) }
        }

        connection.prepare("SELECT unitCost, currency FROM DiaryProduct WHERE id = 1").use {
            statement ->
            statement.step()
            assertTrue { statement.isNull(0) }
            assertTrue { statement.isNull(1) }
        }

        connection.prepare("SELECT unitCost, currency FROM DiaryRecipe WHERE id = 1").use {
            statement ->
            statement.step()
            assertTrue { statement.isNull(0) }
            assertTrue { statement.isNull(1) }
        }

        // Row counts unchanged.
        connection.prepare("SELECT COUNT(*) FROM Measurement").use { statement ->
            statement.step()
            assertTrue { statement.getLong(0) == 2L }
        }
        connection.prepare("SELECT COUNT(*) FROM ManualDiaryEntry").use { statement ->
            statement.step()
            assertTrue { statement.getLong(0) == 1L }
        }

        connection.close()
    }
}
