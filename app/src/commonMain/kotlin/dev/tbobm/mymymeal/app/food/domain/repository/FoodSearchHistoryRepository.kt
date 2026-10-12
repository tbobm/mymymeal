package dev.tbobm.mymymeal.app.food.domain.repository

import dev.tbobm.mymymeal.app.food.domain.entity.FoodSearchHistory
import kotlinx.coroutines.flow.Flow

interface FoodSearchHistoryRepository {
    fun observeHistory(limit: Int): Flow<List<FoodSearchHistory>>

    suspend fun insert(entry: FoodSearchHistory)
}
