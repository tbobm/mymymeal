package dev.tbobm.mymymeal.app.food.search.domain

import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferences

data class FoodSearchPreferences(val openFoodFacts: OpenFoodFacts, val usda: Usda) :
    UserPreferences {
    data class OpenFoodFacts(val enabled: Boolean)

    data class Usda(val enabled: Boolean, val apiKey: String?)

    val isOpenFoodFactsEnabled: Boolean
        get() = openFoodFacts.enabled

    val isUsdaEnabled: Boolean
        get() = usda.enabled
}
