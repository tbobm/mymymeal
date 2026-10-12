package dev.tbobm.mymymeal.app.settings.domain.event

import dev.tbobm.mymymeal.app.changelog.domain.Changelog
import dev.tbobm.mymymeal.app.changelog.domain.ChangelogRepository
import dev.tbobm.mymymeal.app.common.domain.event.IntegrationEventHandler
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.settings.domain.entity.AppLaunchInfo
import dev.tbobm.mymymeal.app.settings.domain.entity.Settings
import kotlinx.coroutines.flow.first

// TODO
//  Where do I put this, it uses settings and changelog modules
internal class AppLaunchEventHandler(
    private val settingsRepository: UserPreferencesRepository<Settings>,
    private val changelogRepository: ChangelogRepository,
) : IntegrationEventHandler<AppLaunchEvent> {
    override suspend fun handle(event: AppLaunchEvent) {
        val changelog = changelogRepository.observe().first()

        settingsRepository.update {
            // If the app version has changed since the last launch, we want reset the preview
            // dialog
            // hidden state, so it can be shown again for the new version if it's a preview.
            val hidePreviewDialog =
                if (lastRememberedVersion != changelog.currentVersion?.version) false
                else this.hidePreviewDialog

            // Update app launch info
            val appLaunchInfo = appLaunchInfo.update(event, changelog)

            copy(appLaunchInfo = appLaunchInfo, hidePreviewDialog = hidePreviewDialog)
        }
    }

    private fun AppLaunchInfo.update(event: AppLaunchEvent, changelog: Changelog): AppLaunchInfo {
        val currentVersion = changelog.currentVersion?.version
        val firstLaunchCurrentVersion =
            if (currentVersion != null && firstLaunchCurrentVersion?.first != currentVersion) {
                currentVersion to event.timestamp
            } else {
                firstLaunchCurrentVersion
            }

        return copy(
            firstLaunch = firstLaunch ?: event.timestamp,
            firstLaunchCurrentVersion = firstLaunchCurrentVersion,
            launchesCount = launchesCount + 1,
        )
    }
}
