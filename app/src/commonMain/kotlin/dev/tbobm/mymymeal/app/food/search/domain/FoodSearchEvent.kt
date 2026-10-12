package dev.tbobm.mymymeal.app.food.search.domain

import dev.tbobm.mymymeal.app.common.domain.event.IntegrationEvent
import dev.tbobm.mymymeal.app.common.domain.search.SearchQuery
import kotlin.time.Instant

data class FoodSearchEvent(val query: SearchQuery.Text, val timestamp: Instant) : IntegrationEvent
