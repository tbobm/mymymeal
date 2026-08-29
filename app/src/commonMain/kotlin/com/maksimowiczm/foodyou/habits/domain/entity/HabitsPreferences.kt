package com.maksimowiczm.foodyou.habits.domain.entity

import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferences

/**
 * The one-time-configured "default coffee" used by the Habits card's one-tap coffee log.
 * `defaultCoffeeFoodId`/`defaultCoffeeIsRecipe` together reconstruct a `FoodId`;
 * `defaultCoffeeMeasurementType`/`Value` together reconstruct a `Measurement` -- DataStore has no
 * native support for either sealed type, so both are split into primitives and reassembled at
 * read time in the call site (see `HabitsCardViewModel`).
 */
data class HabitsPreferences(
    val defaultCoffeeFoodId: Long? = null,
    val defaultCoffeeIsRecipe: Boolean = false,
    val defaultCoffeeMealId: Long? = null,
    val defaultCoffeeMeasurementType: MeasurementType? = null,
    val defaultCoffeeMeasurementValue: Double? = null,
) : UserPreferences
