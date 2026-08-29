package com.maksimowiczm.foodyou.food.domain.repository

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.domain.entity.RecentFood
import kotlinx.coroutines.flow.Flow

interface FoodMeasurementSuggestionRepository {
    suspend fun insert(foodId: FoodId, measurement: Measurement)

    fun observeByFoodId(foodId: FoodId, limit: Int): Flow<List<Measurement>>

    /** Most recently logged foods, each with the measurement it was last logged with. */
    fun observeRecentFoods(limit: Int): Flow<List<RecentFood>>
}
