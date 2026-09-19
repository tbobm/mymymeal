package com.maksimowiczm.foodyou.app.ui.database.healthconnect

import com.maksimowiczm.foodyou.app.infrastructure.android.healthconnect.HealthConnectSyncService
import com.maksimowiczm.foodyou.common.infrastructure.koin.applicationCoroutineScope
import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.onClose

internal actual fun Module.healthConnectModule() {
    single {
        HealthConnectSyncService(
            context = androidContext(),
            observeDiaryMeals = get(),
            dateProvider = get(),
            preferences = userPreferencesRepository(),
        )
    }

    viewModel {
        HealthConnectViewModel(
            context = androidContext(),
            preferencesRepository = userPreferencesRepository(),
            syncService = get(),
        )
    }

    // Runs for the app's lifetime, syncing today whenever the diary changes, as long as the
    // sync-enabled preference is on. No WorkManager dependency: this repo has none, and a
    // process-lifetime coroutine on the existing applicationCoroutineScope covers it.
    single(qualifier = named("HealthConnectLiveSync"), createdAtStart = true) {
            applicationCoroutineScope().launch { get<HealthConnectSyncService>().collectLiveUpdates() }
        }
        .onClose { it?.cancel() }
}
