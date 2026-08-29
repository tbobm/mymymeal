package com.maksimowiczm.foodyou.habits.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.infrastructure.datastore.AbstractDataStoreUserPreferencesRepository
import com.maksimowiczm.foodyou.habits.domain.entity.HabitsPreferences

internal class DataStoreHabitsPreferencesRepository(dataStore: DataStore<Preferences>) :
    AbstractDataStoreUserPreferencesRepository<HabitsPreferences>(dataStore) {
    override fun Preferences.toUserPreferences(): HabitsPreferences =
        HabitsPreferences(
            defaultCoffeeFoodId = this[HabitsPreferencesDataStoreKeys.defaultCoffeeFoodId],
            defaultCoffeeIsRecipe =
                this[HabitsPreferencesDataStoreKeys.defaultCoffeeIsRecipe] ?: false,
            defaultCoffeeMealId = this[HabitsPreferencesDataStoreKeys.defaultCoffeeMealId],
            defaultCoffeeMeasurementType =
                this[HabitsPreferencesDataStoreKeys.defaultCoffeeMeasurementType]?.let {
                    runCatching { MeasurementType.valueOf(it) }.getOrNull()
                },
            defaultCoffeeMeasurementValue =
                this[HabitsPreferencesDataStoreKeys.defaultCoffeeMeasurementValue],
        )

    override fun MutablePreferences.applyUserPreferences(updated: HabitsPreferences) {
        setWithNull(HabitsPreferencesDataStoreKeys.defaultCoffeeFoodId, updated.defaultCoffeeFoodId)
        this[HabitsPreferencesDataStoreKeys.defaultCoffeeIsRecipe] = updated.defaultCoffeeIsRecipe
        setWithNull(HabitsPreferencesDataStoreKeys.defaultCoffeeMealId, updated.defaultCoffeeMealId)
        setWithNull(
            HabitsPreferencesDataStoreKeys.defaultCoffeeMeasurementType,
            updated.defaultCoffeeMeasurementType?.name,
        )
        setWithNull(
            HabitsPreferencesDataStoreKeys.defaultCoffeeMeasurementValue,
            updated.defaultCoffeeMeasurementValue,
        )
    }

    private fun <T> MutablePreferences.setWithNull(key: Preferences.Key<T>, value: T?) {
        if (value != null) this[key] = value else this.remove(key)
    }
}

private object HabitsPreferencesDataStoreKeys {
    val defaultCoffeeFoodId = longPreferencesKey("habits:default_coffee_food_id")
    val defaultCoffeeIsRecipe = booleanPreferencesKey("habits:default_coffee_is_recipe")
    val defaultCoffeeMealId = longPreferencesKey("habits:default_coffee_meal_id")
    val defaultCoffeeMeasurementType = stringPreferencesKey("habits:default_coffee_measurement_type")
    val defaultCoffeeMeasurementValue = doublePreferencesKey("habits:default_coffee_measurement_value")
}
