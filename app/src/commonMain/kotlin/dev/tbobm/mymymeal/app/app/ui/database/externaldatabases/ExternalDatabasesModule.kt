package dev.tbobm.mymymeal.app.app.ui.database.externaldatabases

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

internal fun Module.externalDatabasesModule() {
    viewModel {
        ExternalDatabasesViewModel(foodSearchPreferencesRepository = userPreferencesRepository())
    }
}
