package dev.tbobm.mymymeal.app.food.domain.repository

import dev.tbobm.mymymeal.app.food.domain.entity.FoodHistory
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import kotlinx.coroutines.flow.Flow

interface FoodHistoryRepository {
    suspend fun insert(foodId: FoodId, history: FoodHistory)

    fun observeFoodHistory(foodId: FoodId): Flow<List<FoodHistory>>
}
