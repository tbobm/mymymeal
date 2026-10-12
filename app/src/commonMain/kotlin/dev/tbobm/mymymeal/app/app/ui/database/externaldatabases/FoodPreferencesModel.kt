package dev.tbobm.mymymeal.app.app.ui.database.externaldatabases

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.food.search.domain.FoodSearchPreferences

@Immutable
internal data class FoodPreferencesModel(
    val useOpenFoodFacts: Boolean? = null,
    val useUsda: Boolean? = null,
) {
    constructor(
        domain: FoodSearchPreferences
    ) : this(useOpenFoodFacts = domain.isOpenFoodFactsEnabled, useUsda = domain.isUsdaEnabled)
}
