package com.maksimowiczm.foodyou.fooddiary.domain.healthexport

import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferences

/**
 * Whether the diary syncs its nutrition totals to Health Connect, and when it last did.
 *
 * Turning [syncEnabled] off does not delete records already written — they are the user's own
 * data, already shared with the rest of the Android health ecosystem. Health Connect's own UI can
 * remove them if the user wants that.
 */
data class HealthConnectPreferences(val syncEnabled: Boolean, val lastSyncedEpochSeconds: Long?) :
    UserPreferences {
    companion object {
        val default = HealthConnectPreferences(syncEnabled = false, lastSyncedEpochSeconds = null)
    }
}
