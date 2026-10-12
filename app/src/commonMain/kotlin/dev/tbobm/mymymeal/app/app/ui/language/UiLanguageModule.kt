package dev.tbobm.mymymeal.app.app.ui.language

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.language() {
    viewModel {
        LanguageViewModel(
            translationRepository = get(),
            settingsRepository = userPreferencesRepository(),
        )
    }
}
