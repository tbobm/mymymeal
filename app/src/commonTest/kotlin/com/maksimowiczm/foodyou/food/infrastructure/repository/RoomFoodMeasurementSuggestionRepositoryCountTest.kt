package com.maksimowiczm.foodyou.food.infrastructure.repository

import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.test.core.app.ApplicationProvider
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.infrastructure.room.MeasurementSuggestionEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomFoodMeasurementSuggestionRepositoryCountTest {
    private suspend fun buildDatabase(): FoodYouDatabase {
        val db =
            Room.inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    FoodYouDatabase::class.java,
                )
                .build()
        db.useWriterConnection { connection ->
            connection.usePrepared(
                "INSERT INTO Product (id, name, sourceType, isLiquid) VALUES (1, 'Coffee', 0, 0)"
            ) {
                it.step()
            }
            connection.usePrepared(
                "INSERT INTO Product (id, name, sourceType, isLiquid) VALUES (2, 'Tea', 0, 0)"
            ) {
                it.step()
            }
        }
        return db
    }

    @Test
    fun `counts only suggestions for the given food within the day range`() = runTest {
        val db = buildDatabase()
        val dao = db.measurementSuggestionDao
        val repository = RoomFoodMeasurementSuggestionRepository(dao)
        val coffeeId = FoodId.Product(1)

        dao.insert(
            MeasurementSuggestionEntity(
                productId = 1,
                recipeId = null,
                epochSeconds = 1_000,
                type = MeasurementType.Milliliter,
                value = 250.0,
            )
        )
        dao.insert(
            MeasurementSuggestionEntity(
                productId = 1,
                recipeId = null,
                epochSeconds = 2_000,
                type = MeasurementType.Milliliter,
                value = 250.0,
            )
        )
        dao.insert(
            // outside the queried range
            MeasurementSuggestionEntity(
                productId = 1,
                recipeId = null,
                epochSeconds = 99_999,
                type = MeasurementType.Milliliter,
                value = 250.0,
            )
        )
        dao.insert(
            // different product (Tea, id 2) -- must not count toward Coffee's total
            MeasurementSuggestionEntity(
                productId = 2,
                recipeId = null,
                epochSeconds = 1_500,
                type = MeasurementType.Milliliter,
                value = 250.0,
            )
        )

        val count =
            repository
                .observeCountByFoodId(coffeeId, sinceEpochSeconds = 0, untilEpochSeconds = 10_000)
                .first()

        assertEquals(2, count)
    }
}
