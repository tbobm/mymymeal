package dev.tbobm.mymymeal.app.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import dev.tbobm.mymymeal.app.common.infrastructure.datastore.AbstractDataStoreUserPreferencesRepository
import dev.tbobm.mymymeal.app.common.infrastructure.datastore.set

internal class DataStoreNutrientsColorsRepository(dataStore: DataStore<Preferences>) :
    AbstractDataStoreUserPreferencesRepository<NutrientsColors>(dataStore) {
    override fun Preferences.toUserPreferences(): NutrientsColors {
        return NutrientsColors(
            proteins = this[NutrientsColorsPreferencesKeys.proteinsColor]?.toULong(),
            carbohydrates = this[NutrientsColorsPreferencesKeys.carbohydratesColor]?.toULong(),
            fats = this[NutrientsColorsPreferencesKeys.fatsColor]?.toULong(),
        )
    }

    override fun MutablePreferences.applyUserPreferences(updated: NutrientsColors) {
        this[NutrientsColorsPreferencesKeys.proteinsColor] = updated.proteins?.toLong()
        this[NutrientsColorsPreferencesKeys.carbohydratesColor] = updated.carbohydrates?.toLong()
        this[NutrientsColorsPreferencesKeys.fatsColor] = updated.fats?.toLong()
    }
}

private object NutrientsColorsPreferencesKeys {
    val proteinsColor = longPreferencesKey("nutrientsColors:proteins")
    val carbohydratesColor = longPreferencesKey("nutrientsColors:carbohydrates")
    val fatsColor = longPreferencesKey("nutrientsColors:fats")
}
