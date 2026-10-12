package dev.tbobm.mymymeal.app.common.domain.userpreferences

import kotlinx.coroutines.flow.first

suspend fun <P : UserPreferences> UserPreferencesRepository<P>.get(): P = observe().first()
