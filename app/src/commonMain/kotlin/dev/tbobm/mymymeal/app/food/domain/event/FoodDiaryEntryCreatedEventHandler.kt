package dev.tbobm.mymymeal.app.food.domain.event

import dev.tbobm.mymymeal.app.common.domain.event.IntegrationEventHandler
import dev.tbobm.mymymeal.app.food.domain.repository.FoodMeasurementSuggestionRepository
import dev.tbobm.mymymeal.app.fooddiary.domain.event.FoodDiaryEntryCreatedEvent

internal class FoodDiaryEntryCreatedEventHandler(
    private val repository: FoodMeasurementSuggestionRepository
) : IntegrationEventHandler<FoodDiaryEntryCreatedEvent> {
    override suspend fun handle(event: FoodDiaryEntryCreatedEvent) {
        repository.insert(foodId = event.foodId, measurement = event.measurement)
    }
}
