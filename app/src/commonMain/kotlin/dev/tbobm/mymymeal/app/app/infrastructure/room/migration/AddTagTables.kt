package dev.tbobm.mymymeal.app.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * PRD 3.5 (categorisation). Adds a user-defined `Tag` table plus many-to-many cross-reference
 * tables for what tags actually attach to: `Product`, `Recipe`, and `ManualDiaryEntry`.
 * `Measurement` is intentionally not given a cross-ref -- it already snapshots/references a
 * product or recipe via `originProductId`/`originRecipeId`, so tagging the food covers it
 * transitively. Purely additive, no existing table or column is altered.
 */
internal val addTagTables =
    object : Migration(33, 34) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `Tag` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL)"
            )
            connection.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_Tag_name` ON `Tag` (`name`)"
            )

            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `ProductTagCrossRef` (" +
                    "`productId` INTEGER NOT NULL, " +
                    "`tagId` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`productId`, `tagId`), " +
                    "FOREIGN KEY(`productId`) REFERENCES `Product`(`id`) ON UPDATE NO ACTION " +
                    "ON DELETE CASCADE, " +
                    "FOREIGN KEY(`tagId`) REFERENCES `Tag`(`id`) ON UPDATE NO ACTION " +
                    "ON DELETE CASCADE)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_ProductTagCrossRef_productId` " +
                    "ON `ProductTagCrossRef` (`productId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_ProductTagCrossRef_tagId` " +
                    "ON `ProductTagCrossRef` (`tagId`)"
            )

            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `RecipeTagCrossRef` (" +
                    "`recipeId` INTEGER NOT NULL, " +
                    "`tagId` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`recipeId`, `tagId`), " +
                    "FOREIGN KEY(`recipeId`) REFERENCES `Recipe`(`id`) ON UPDATE NO ACTION " +
                    "ON DELETE CASCADE, " +
                    "FOREIGN KEY(`tagId`) REFERENCES `Tag`(`id`) ON UPDATE NO ACTION " +
                    "ON DELETE CASCADE)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_RecipeTagCrossRef_recipeId` " +
                    "ON `RecipeTagCrossRef` (`recipeId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_RecipeTagCrossRef_tagId` " +
                    "ON `RecipeTagCrossRef` (`tagId`)"
            )

            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `ManualDiaryEntryTagCrossRef` (" +
                    "`manualDiaryEntryId` INTEGER NOT NULL, " +
                    "`tagId` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`manualDiaryEntryId`, `tagId`), " +
                    "FOREIGN KEY(`manualDiaryEntryId`) REFERENCES `ManualDiaryEntry`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`tagId`) REFERENCES `Tag`(`id`) ON UPDATE NO ACTION " +
                    "ON DELETE CASCADE)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                    "`index_ManualDiaryEntryTagCrossRef_manualDiaryEntryId` " +
                    "ON `ManualDiaryEntryTagCrossRef` (`manualDiaryEntryId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_ManualDiaryEntryTagCrossRef_tagId` " +
                    "ON `ManualDiaryEntryTagCrossRef` (`tagId`)"
            )
        }
    }
