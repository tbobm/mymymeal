package dev.tbobm.mymymeal.app.settings.infrastructure

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepositoryOf
import dev.tbobm.mymymeal.app.settings.domain.repository.TranslationRepository
import org.koin.core.module.Module
import org.koin.dsl.bind

internal fun Module.settingsInfrastructureModule() {
    userPreferencesRepositoryOf(::DataStoreSettingsRepository)
    factory {
            TranslationRepositoryImpl(
                systemDetails = get(),
                settingsRepository = userPreferencesRepository(),
            )
        }
        .bind<TranslationRepository>()
}
