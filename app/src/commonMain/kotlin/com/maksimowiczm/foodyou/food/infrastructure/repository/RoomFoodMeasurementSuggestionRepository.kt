package com.maksimowiczm.foodyou.food.infrastructure.repository

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.domain.entity.RecentFood
import com.maksimowiczm.foodyou.food.domain.repository.FoodMeasurementSuggestionRepository
import com.maksimowiczm.foodyou.food.infrastructure.room.MeasurementSuggestionDao
import com.maksimowiczm.foodyou.food.infrastructure.room.MeasurementSuggestionEntity
import com.maksimowiczm.foodyou.food.infrastructure.room.RecentFoodSuggestion
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomFoodMeasurementSuggestionRepository(
    private val measurementSuggestionDao: MeasurementSuggestionDao
) : FoodMeasurementSuggestionRepository {
    override suspend fun insert(foodId: FoodId, measurement: Measurement) {
        measurementSuggestionDao.insert(measurement.toEntity(foodId))
    }

    override fun observeByFoodId(foodId: FoodId, limit: Int): Flow<List<Measurement>> =
        measurementSuggestionDao.observeByFoodId(foodId, limit).map { list ->
            list.map(MeasurementSuggestionEntity::toMeasurement)
        }

    override fun observeRecentFoods(limit: Int): Flow<List<RecentFood>> =
        measurementSuggestionDao.observeRecentFoods(limit).map { list ->
            list.map(RecentFoodSuggestion::toRecentFood)
        }
}

private fun MeasurementSuggestionDao.observeByFoodId(
    foodId: FoodId,
    limit: Int,
): Flow<List<MeasurementSuggestionEntity>> =
    this.observeByFoodId(
        productId = (foodId as? FoodId.Product)?.id,
        recipeId = (foodId as? FoodId.Recipe)?.id,
        limit = limit,
    )

private fun Measurement.toEntity(foodId: FoodId, now: Instant = Clock.System.now()) =
    MeasurementSuggestionEntity(
        productId = (foodId as? FoodId.Product)?.id,
        recipeId = (foodId as? FoodId.Recipe)?.id,
        epochSeconds = now.epochSeconds,
        type = type,
        value = rawValue,
    )

private fun MeasurementSuggestionEntity.toMeasurement(): Measurement =
    Measurement.from(type = type, rawValue = value)

private fun RecentFoodSuggestion.toRecentFood(): RecentFood =
    RecentFood(
        foodId = productId?.let(FoodId::Product) ?: FoodId.Recipe(recipeId!!),
        headline = headline,
        measurement = Measurement.from(type = type, rawValue = value),
    )
