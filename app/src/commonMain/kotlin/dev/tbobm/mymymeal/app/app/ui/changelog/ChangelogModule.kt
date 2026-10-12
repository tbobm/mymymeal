package dev.tbobm.mymymeal.app.app.ui.changelog

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.changelog() {
    viewModel {
        ChangelogViewModel(
            changelogRepository = get(),
            settingsRepository = userPreferencesRepository(),
            savedStateHandle = get(),
        )
    }
}
