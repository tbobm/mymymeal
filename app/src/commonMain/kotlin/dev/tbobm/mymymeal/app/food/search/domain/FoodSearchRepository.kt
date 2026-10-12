package dev.tbobm.mymymeal.app.food.search.domain

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.tbobm.mymymeal.app.common.domain.food.FoodSource
import dev.tbobm.mymymeal.app.common.domain.search.SearchQuery
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface FoodSearchRepository {
    fun search(
        query: SearchQuery,
        source: FoodSource.Type,
        config: PagingConfig,
        remoteMediatorFactory: RemoteMediatorFactory?,
        excludedRecipeId: FoodId.Recipe?,
    ): Flow<PagingData<FoodSearch>>

    fun searchRecent(
        query: SearchQuery,
        config: PagingConfig,
        now: LocalDateTime,
        excludedRecipeId: FoodId.Recipe?,
    ): Flow<PagingData<FoodSearch>>

    fun searchFoodCount(
        query: SearchQuery,
        source: FoodSource.Type,
        excludedRecipeId: FoodId.Recipe?,
    ): Flow<Int>

    fun searchRecentFoodCount(
        query: SearchQuery,
        now: LocalDateTime,
        excludedRecipeId: FoodId.Recipe?,
    ): Flow<Int>
}
