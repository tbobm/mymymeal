package com.maksimowiczm.foodyou.fooddiary.infrastructure.healthexport

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.maksimowiczm.foodyou.common.infrastructure.datastore.AbstractDataStoreUserPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.healthexport.HealthConnectPreferences

internal class DataStoreHealthConnectPreferencesRepository(dataStore: DataStore<Preferences>) :
    AbstractDataStoreUserPreferencesRepository<HealthConnectPreferences>(dataStore) {
    override fun Preferences.toUserPreferences(): HealthConnectPreferences =
        HealthConnectPreferences(
            syncEnabled = this[HealthConnectPreferencesDataStoreKeys.syncEnabled]
                ?: HealthConnectPreferences.default.syncEnabled,
            lastSyncedEpochSeconds = this[HealthConnectPreferencesDataStoreKeys.lastSyncedEpochSeconds],
        )

    override fun MutablePreferences.applyUserPreferences(updated: HealthConnectPreferences) {
        this[HealthConnectPreferencesDataStoreKeys.syncEnabled] = updated.syncEnabled
        val lastSynced = updated.lastSyncedEpochSeconds
        if (lastSynced != null) {
            this[HealthConnectPreferencesDataStoreKeys.lastSyncedEpochSeconds] = lastSynced
        }
    }
}

private object HealthConnectPreferencesDataStoreKeys {
    val syncEnabled = booleanPreferencesKey("fooddiary:health_connect:sync_enabled")
    val lastSyncedEpochSeconds =
        longPreferencesKey("fooddiary:health_connect:last_synced_epoch_seconds")
}
