package dev.tbobm.mymymeal.app.food.domain.repository

import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import kotlinx.coroutines.flow.Flow

interface FoodMeasurementSuggestionRepository {
    suspend fun insert(foodId: FoodId, measurement: Measurement)

    fun observeByFoodId(foodId: FoodId, limit: Int): Flow<List<Measurement>>
}
