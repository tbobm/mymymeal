package dev.tbobm.mymymeal.app.app.infrastructure.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.immediateTransaction
import androidx.room.migration.Migration
import androidx.room.useWriterConnection
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.FoodSearchFtsCyrillicMigration
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.FoodSearchFtsMigration
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.LegacyMigrations
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.addCoffeeIntakeTable
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.addHabitsTables
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.addProvenanceAndCostColumns
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.addSupplementDose
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.addTagTables
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.addUsageLogTables
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.deleteUsedFoodEvent
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.fixMeasurementSuggestions
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.mymymeal3Migration
import dev.tbobm.mymymeal.app.app.infrastructure.room.migration.unlinkDiaryMigration
import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.database.TransactionScope as DomainTransactionScope
import dev.tbobm.mymymeal.app.common.infrastructure.room.FoodSourceTypeConverter
import dev.tbobm.mymymeal.app.common.infrastructure.room.MeasurementTypeConverter
import dev.tbobm.mymymeal.app.common.infrastructure.room.RoomTransactionScope
import dev.tbobm.mymymeal.app.common.infrastructure.room.tag.ManualDiaryEntryTagCrossRefEntity
import dev.tbobm.mymymeal.app.common.infrastructure.room.tag.ProductTagCrossRefEntity
import dev.tbobm.mymymeal.app.common.infrastructure.room.tag.RecipeTagCrossRefEntity
import dev.tbobm.mymymeal.app.common.infrastructure.room.tag.TagDatabase
import dev.tbobm.mymymeal.app.common.infrastructure.room.tag.TagEntity
import dev.tbobm.mymymeal.app.food.infrastructure.room.FoodDatabase
import dev.tbobm.mymymeal.app.food.infrastructure.room.FoodEventEntity
import dev.tbobm.mymymeal.app.food.infrastructure.room.FoodEventTypeConverter
import dev.tbobm.mymymeal.app.food.infrastructure.room.LatestMeasurementSuggestion
import dev.tbobm.mymymeal.app.food.infrastructure.room.MeasurementSuggestionEntity
import dev.tbobm.mymymeal.app.food.infrastructure.room.ProductEntity
import dev.tbobm.mymymeal.app.food.infrastructure.room.ProductFts
import dev.tbobm.mymymeal.app.food.infrastructure.room.RecipeEntity
import dev.tbobm.mymymeal.app.food.infrastructure.room.RecipeFts
import dev.tbobm.mymymeal.app.food.infrastructure.room.RecipeIngredientEntity
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.FoodSearchDatabase
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.OpenFoodFactsPagingKeyEntity
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.RecipeAllIngredientsView
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.SearchEntry
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.USDAPagingKeyEntity
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.DiaryProductEntity
import dev.tbobm.mymymeal.app.habits.infrastructure.room.CoffeeIntakeEntity
import dev.tbobm.mymymeal.app.habits.infrastructure.room.HabitsDatabase
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.DiaryRecipeEntity
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.DiaryRecipeIngredientEntity
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.FoodDiaryDatabase
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.InitializeMealsCallback
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.ManualDiaryEntryEntity
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.MealEntity
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.MeasurementEntity
import dev.tbobm.mymymeal.app.habits.infrastructure.room.SupplementEntity
import dev.tbobm.mymymeal.app.habits.infrastructure.room.SupplementIntakeEntity
import dev.tbobm.mymymeal.app.sponsorship.infrastructure.room.SponsorshipDatabase
import dev.tbobm.mymymeal.app.sponsorship.infrastructure.room.SponsorshipEntity
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageDailyEntity
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageEventEntity
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageLogDatabase

@Database(
    entities =
        [
            ProductEntity::class,
            RecipeEntity::class,
            RecipeIngredientEntity::class,
            OpenFoodFactsPagingKeyEntity::class,
            USDAPagingKeyEntity::class,
            FoodEventEntity::class,
            SearchEntry::class,
            MealEntity::class,
            MeasurementEntity::class,
            DiaryProductEntity::class,
            DiaryRecipeEntity::class,
            DiaryRecipeIngredientEntity::class,
            SponsorshipEntity::class,
            MeasurementSuggestionEntity::class,
            ManualDiaryEntryEntity::class,
            ProductFts::class,
            RecipeFts::class,
            TagEntity::class,
            ProductTagCrossRefEntity::class,
            RecipeTagCrossRefEntity::class,
            ManualDiaryEntryTagCrossRefEntity::class,
            SupplementEntity::class,
            SupplementIntakeEntity::class,
            CoffeeIntakeEntity::class,
            UsageEventEntity::class,
            UsageDailyEntity::class,
        ],
    views = [RecipeAllIngredientsView::class, LatestMeasurementSuggestion::class],
    version = MymymealDatabase.VERSION,
    exportSchema = true,
    autoMigrations =
        [
            /** @see [LegacyMigrations.MIGRATION_1_2] Add rank to MealEntity */
            /** @see [LegacyMigrations.MIGRATION_2_3] 2.0.0 schema change */
            AutoMigration(from = 3, to = 4),
            AutoMigration(from = 4, to = 5),
            AutoMigration(from = 5, to = 6),
            AutoMigration(from = 6, to = 7),
            /**
             * @see [LegacyMigrations.MIGRATION_7_8] Remove unused products from OpenFoodFacts
             *   source
             */
            /** @see [LegacyMigrations.MIGRATION_8_9] Remove OpenFoodFactsPagingKeyEntity */
            AutoMigration(from = 9, to = 10, spec = LegacyMigrations.MIGRATION_9_10::class),
            AutoMigration(from = 10, to = 11),
            /**
             * @see [LegacyMigrations.MIGRATION_11_12] Fix sodium value in ProductEntity. Convert
             *   grams to milligrams.
             */
            AutoMigration(from = 12, to = 13),
            AutoMigration(from = 13, to = 14),
            AutoMigration(from = 14, to = 15),
            AutoMigration(from = 15, to = 16),
            AutoMigration(from = 16, to = 17),
            AutoMigration(from = 17, to = 18),
            /**
             * @see [LegacyMigrations.MIGRATION_18_19] Merge product and recipe measurements into
             *   MeasurementEntity
             */
            AutoMigration(from = 19, to = 20),
            /**
             * @see [LegacyMigrations.MIGRATION_20_21] Add isLiquid column to ProductEntity and
             *   RecipeEntity
             */
            /**
             * @see [LegacyMigrations.MIGRATION_21_22] Add `note` column to ProductEntity and
             *   RecipeEntity
             */
            AutoMigration(from = 23, to = 24), // Add LatestFoodMeasuredEventView
            AutoMigration(from = 24, to = 25), // Add FoodEventEntity onDelete cascade
            AutoMigration(from = 28, to = 29), // Add ManualDiaryEntryEntity
            AutoMigration(from = 29, to = 30), // Add MeasurementSuggestion indices
            /** @see [FoodSearchFtsMigration] Add FTS tables for ProductEntity and RecipeEntity */
            /**
             * @see [FoodSearchFtsCyrillicMigration] Add Cyrillic tokenizer support to FTS tables
             */
        ],
)
@TypeConverters(
    FoodSourceTypeConverter::class,
    MeasurementTypeConverter::class,
    FoodEventTypeConverter::class,
)
abstract class MymymealDatabase :
    RoomDatabase(),
    TransactionProvider,
    FoodDatabase,
    FoodSearchDatabase,
    FoodDiaryDatabase,
    SponsorshipDatabase,
    TagDatabase,
    HabitsDatabase,
    UsageLogDatabase {

    override suspend fun <T> withTransaction(block: suspend DomainTransactionScope<T>.() -> T): T =
        useWriterConnection {
            it.immediateTransaction {
                val scope = RoomTransactionScope<T>(this)
                scope.block()
            }
        }

    companion object {
        const val VERSION = 38

        private val migrations: List<Migration> =
            listOf(
                LegacyMigrations.MIGRATION_1_2,
                LegacyMigrations.MIGRATION_2_3,
                LegacyMigrations.MIGRATION_7_8,
                LegacyMigrations.MIGRATION_8_9,
                LegacyMigrations.MIGRATION_11_12,
                LegacyMigrations.MIGRATION_18_19,
                LegacyMigrations.MIGRATION_20_21,
                LegacyMigrations.MIGRATION_21_22,
                mymymeal3Migration,
                unlinkDiaryMigration,
                deleteUsedFoodEvent,
                fixMeasurementSuggestions,
                FoodSearchFtsMigration,
                FoodSearchFtsCyrillicMigration,
                addProvenanceAndCostColumns,
                addTagTables,
                addHabitsTables,
                addSupplementDose,
                addCoffeeIntakeTable,
                addUsageLogTables,
            )

        fun Builder<MymymealDatabase>.buildDatabase(
            mealsCallback: InitializeMealsCallback
        ): MymymealDatabase {
            addMigrations(*migrations.toTypedArray())
            addCallback(mealsCallback)
            return build()
        }
    }
}
