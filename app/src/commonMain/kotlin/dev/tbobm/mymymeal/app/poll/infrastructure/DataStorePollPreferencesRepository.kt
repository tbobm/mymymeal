package dev.tbobm.mymymeal.app.poll.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.tbobm.mymymeal.app.common.infrastructure.datastore.AbstractDataStoreUserPreferencesRepository
import dev.tbobm.mymymeal.app.poll.domain.entity.PollId
import dev.tbobm.mymymeal.app.poll.domain.entity.PollPreferences

internal class DataStorePollPreferencesRepository(dataStore: DataStore<Preferences>) :
    AbstractDataStoreUserPreferencesRepository<PollPreferences>(dataStore) {
    override fun Preferences.toUserPreferences(): PollPreferences {
        val dismissedPolls = this[PollPreferencesKeys.dismissedPools] ?: emptySet()
        return PollPreferences(dismissedPolls.map(String::toPollId).toSet())
    }

    override fun MutablePreferences.applyUserPreferences(updated: PollPreferences) {
        this[PollPreferencesKeys.dismissedPools] = updated.dismissedPolls.map { it.value }.toSet()
    }
}

private fun String.toPollId(): PollId = PollId(this)

private object PollPreferencesKeys {
    val dismissedPools = stringSetPreferencesKey("dismissed_polls")
}
