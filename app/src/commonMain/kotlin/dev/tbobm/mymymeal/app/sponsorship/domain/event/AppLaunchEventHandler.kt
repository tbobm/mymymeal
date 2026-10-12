package dev.tbobm.mymymeal.app.sponsorship.domain.event

import dev.tbobm.mymymeal.app.common.domain.event.IntegrationEventHandler
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.common.domain.userpreferences.get
import dev.tbobm.mymymeal.app.settings.domain.event.AppLaunchEvent
import dev.tbobm.mymymeal.app.sponsorship.domain.entity.SponsorshipPreferences
import dev.tbobm.mymymeal.app.sponsorship.domain.repository.SponsorRepository

class AppLaunchEventHandler(
    private val sponsorshipPreferencesRepository: UserPreferencesRepository<SponsorshipPreferences>,
    private val sponsorshipRepository: SponsorRepository,
) : IntegrationEventHandler<AppLaunchEvent> {
    override suspend fun handle(event: AppLaunchEvent) {
        val prefs = sponsorshipPreferencesRepository.get()
        if (prefs.shouldCleanLegacyEntities) {
            sponsorshipRepository.deleteAll()
            sponsorshipPreferencesRepository.update { copy(shouldCleanLegacyEntities = false) }
        }
    }
}
