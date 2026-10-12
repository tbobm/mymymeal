package dev.tbobm.mymymeal.app.fooddiary.domain.event

import dev.tbobm.mymymeal.app.common.domain.event.IntegrationEvent
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import kotlin.time.Instant

data class FoodDiaryEntryCreatedEvent(
    val foodId: FoodId,
    val timestamp: Instant,
    val measurement: Measurement,
) : IntegrationEvent
