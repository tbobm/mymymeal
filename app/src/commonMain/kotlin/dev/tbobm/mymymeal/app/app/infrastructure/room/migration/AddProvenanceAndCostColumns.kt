package dev.tbobm.mymymeal.app.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * PRD 1.2 (provenance) + 1.3 (cost columns), combined per
 * `docs/phase-1.2-1.3-proposal.md`. Purely additive -- no existing column is altered, dropped, or
 * reinterpreted.
 */
internal val addProvenanceAndCostColumns =
    object : Migration(32, 33) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE Measurement ADD COLUMN sourceKind TEXT")
            connection.execSQL("ALTER TABLE Measurement ADD COLUMN confidence TEXT")
            connection.execSQL("ALTER TABLE Measurement ADD COLUMN originProductId INTEGER")
            connection.execSQL("ALTER TABLE Measurement ADD COLUMN originRecipeId INTEGER")
            // Recipe-backed rows are deterministically knowable from the existing recipeId FK.
            // Everything else (measured vs. estimated, which UI action logged it) has no
            // historical record and stays NULL.
            connection.execSQL(
                "UPDATE Measurement SET sourceKind = 'recipe' WHERE recipeId IS NOT NULL"
            )

            // Every row in ManualDiaryEntry is a manual estimate by definition.
            connection.execSQL(
                "ALTER TABLE ManualDiaryEntry ADD COLUMN sourceKind TEXT NOT NULL " +
                    "DEFAULT 'manual_estimate'"
            )
            connection.execSQL(
                "ALTER TABLE ManualDiaryEntry ADD COLUMN confidence TEXT NOT NULL " +
                    "DEFAULT 'estimated'"
            )
            connection.execSQL("ALTER TABLE ManualDiaryEntry ADD COLUMN unitCost REAL")
            connection.execSQL("ALTER TABLE ManualDiaryEntry ADD COLUMN currency TEXT")

            connection.execSQL("ALTER TABLE Product ADD COLUMN pricePerUnit REAL")
            connection.execSQL("ALTER TABLE Product ADD COLUMN currency TEXT")

            connection.execSQL("ALTER TABLE DiaryProduct ADD COLUMN unitCost REAL")
            connection.execSQL("ALTER TABLE DiaryProduct ADD COLUMN currency TEXT")

            connection.execSQL("ALTER TABLE DiaryRecipe ADD COLUMN unitCost REAL")
            connection.execSQL("ALTER TABLE DiaryRecipe ADD COLUMN currency TEXT")
        }
    }
