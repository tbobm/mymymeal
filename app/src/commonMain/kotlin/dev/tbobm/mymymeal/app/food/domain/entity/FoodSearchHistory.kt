package dev.tbobm.mymymeal.app.food.domain.entity

import dev.tbobm.mymymeal.app.common.domain.search.SearchQuery
import kotlin.time.Instant

data class FoodSearchHistory(val timestamp: Instant, val query: SearchQuery.Text)
