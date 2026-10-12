package dev.tbobm.mymymeal.app.food.search.domain

import dev.tbobm.mymymeal.app.common.domain.event.IntegrationEventHandler
import dev.tbobm.mymymeal.app.food.domain.entity.FoodSearchHistory
import dev.tbobm.mymymeal.app.food.domain.repository.FoodSearchHistoryRepository

class FoodSearchEventHandler(private val repository: FoodSearchHistoryRepository) :
    IntegrationEventHandler<FoodSearchEvent> {
    override suspend fun handle(event: FoodSearchEvent) {
        repository.insert(event.toSearchHistory())
    }
}

private fun FoodSearchEvent.toSearchHistory(): FoodSearchHistory =
    FoodSearchHistory(timestamp, query)
