package com.maksimowiczm.foodyou.fooddiary.infrastructure.repository

import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.test.core.app.ApplicationProvider
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.infrastructure.room.FoodSourceType
import com.maksimowiczm.foodyou.common.infrastructure.room.Minerals
import com.maksimowiczm.foodyou.common.infrastructure.room.Nutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.Vitamins
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiaryProductEntity
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.MeasurementEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomFoodDiaryEntryRepositoryEntryCountTest {
    private val emptyNutrients =
        Nutrients(
            energy = null,
            proteins = null,
            fats = null,
            saturatedFats = null,
            transFats = null,
            monounsaturatedFats = null,
            polyunsaturatedFats = null,
            omega3 = null,
            omega6 = null,
            carbohydrates = null,
            sugars = null,
            addedSugars = null,
            dietaryFiber = null,
            solubleFiber = null,
            insolubleFiber = null,
            salt = null,
            cholesterolMilli = null,
            caffeineMilli = null,
        )

    private val emptyVitamins =
        Vitamins(
            vitaminAMicro = null,
            vitaminB1Milli = null,
            vitaminB2Milli = null,
            vitaminB3Milli = null,
            vitaminB5Milli = null,
            vitaminB6Milli = null,
            vitaminB7Micro = null,
            vitaminB9Micro = null,
            vitaminB12Micro = null,
            vitaminCMilli = null,
            vitaminDMicro = null,
            vitaminEMilli = null,
            vitaminKMicro = null,
        )

    private val emptyMinerals =
        Minerals(
            manganeseMilli = null,
            magnesiumMilli = null,
            potassiumMilli = null,
            calciumMilli = null,
            copperMilli = null,
            zincMilli = null,
            sodiumMilli = null,
            ironMilli = null,
            phosphorusMilli = null,
            seleniumMicro = null,
            iodineMicro = null,
            chromiumMicro = null,
        )

    // A Meal row is required -- Measurement has an enforced FK to Meal (onDelete = CASCADE), and
    // Room's in-memory DB has FK checks on by default. MealDao's insert is protected/rank-managed,
    // so a raw insert is simplest here (mirrors the raw-SQL Product insert in the now-deleted
    // RoomFoodMeasurementSuggestionRepositoryCountTest this test replaces).
    private suspend fun buildDatabase(): FoodYouDatabase {
        val db =
            Room.inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    FoodYouDatabase::class.java,
                )
                .build()
        db.useWriterConnection { connection ->
            connection.usePrepared(
                "INSERT INTO Meal (id, name, fromHour, fromMinute, toHour, toMinute, rank) " +
                    "VALUES (1, 'Breakfast', 0, 0, 23, 59, 0)"
            ) {
                it.step()
            }
        }
        return db
    }

    private fun diaryProduct(name: String) =
        DiaryProductEntity(
            name = name,
            nutrients = emptyNutrients,
            vitamins = emptyVitamins,
            minerals = emptyMinerals,
            packageWeight = null,
            servingWeight = null,
            isLiquid = false,
            sourceType = FoodSourceType.User,
            sourceUrl = null,
            note = null,
        )

    @Test
    fun `counts today's entries for the given product name only`() = runTest {
        val db = buildDatabase()
        val dao = db.measurementDao
        val repository = RoomFoodDiaryEntryRepository(db, dao)
        val today = LocalDate(2026, 8, 29)
        val yesterday = LocalDate(2026, 8, 28)
        val mealId = 1L

        val coffeeId = dao.insertDiaryProduct(diaryProduct("Coffee"))
        val teaId = dao.insertDiaryProduct(diaryProduct("Tea"))

        fun measurement(productId: Long, epochDay: Long) =
            MeasurementEntity(
                mealId = mealId,
                epochDay = epochDay,
                productId = productId,
                recipeId = null,
                measurement = MeasurementType.Milliliter,
                quantity = 250.0,
                createdAt = 0,
                updatedAt = 0,
            )

        dao.insertMeasurement(measurement(coffeeId, today.toEpochDays()))
        dao.insertMeasurement(measurement(coffeeId, today.toEpochDays()))
        // different product -- must not count toward Coffee's total
        dao.insertMeasurement(measurement(teaId, today.toEpochDays()))
        // same product, different (earlier) day -- must not count
        dao.insertMeasurement(measurement(coffeeId, yesterday.toEpochDays()))

        val count =
            repository.observeEntryCountByFoodName("Coffee", isRecipe = false, date = today).first()

        assertEquals(2, count)
    }
}
