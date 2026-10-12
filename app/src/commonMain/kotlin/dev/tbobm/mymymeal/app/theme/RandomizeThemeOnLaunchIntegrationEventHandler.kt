package dev.tbobm.mymymeal.app.theme

import dev.tbobm.mymymeal.app.common.domain.event.IntegrationEventHandler
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.common.domain.userpreferences.get
import dev.tbobm.mymymeal.app.settings.domain.event.AppLaunchEvent

internal class RandomizeThemeOnLaunchIntegrationEventHandler(
    private val themeSettingsRepository: UserPreferencesRepository<ThemeSettings>,
    private val randomizeThemeUseCase: RandomizeThemeUseCase,
) : IntegrationEventHandler<AppLaunchEvent> {
    override suspend fun handle(event: AppLaunchEvent) {
        if (themeSettingsRepository.get().randomizeOnLaunch) {
            randomizeThemeUseCase.randomize()
        }
    }
}
