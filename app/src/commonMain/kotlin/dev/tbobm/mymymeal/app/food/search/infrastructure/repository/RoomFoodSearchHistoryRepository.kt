package dev.tbobm.mymymeal.app.food.search.infrastructure.repository

import dev.tbobm.mymymeal.app.common.domain.search.SearchQuery
import dev.tbobm.mymymeal.app.food.domain.entity.FoodSearchHistory
import dev.tbobm.mymymeal.app.food.domain.repository.FoodSearchHistoryRepository
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.FoodSearchDao
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.SearchEntry
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomFoodSearchHistoryRepository(private val foodSearchDao: FoodSearchDao) :
    FoodSearchHistoryRepository {
    override fun observeHistory(limit: Int): Flow<List<FoodSearchHistory>> =
        foodSearchDao.observeRecentSearches(limit).map { list -> list.map { it.toModel() } }

    override suspend fun insert(entry: FoodSearchHistory) {
        foodSearchDao.insertSearchEntry(entry.toEntity())
    }
}

private fun SearchEntry.toModel(): FoodSearchHistory =
    FoodSearchHistory(
        timestamp = epochSeconds.let(Instant::fromEpochSeconds),
        query = SearchQuery.Text(query),
    )

private fun FoodSearchHistory.toEntity(): SearchEntry =
    SearchEntry(epochSeconds = timestamp.epochSeconds, query = query.query)
