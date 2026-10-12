package dev.tbobm.mymymeal.app.theme

import dev.tbobm.mymymeal.app.common.infrastructure.koin.eventHandler
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepositoryOf
import org.koin.dsl.module

val themeModule = module {
    userPreferencesRepositoryOf(::DataStoreThemeSettingsRepository)
    userPreferencesRepositoryOf(::DataStoreNutrientsColorsRepository)
    factory { RandomizeThemeUseCase(userPreferencesRepository(), get()) }
    eventHandler {
        RandomizeThemeOnLaunchIntegrationEventHandler(userPreferencesRepository(), get())
    }
}
