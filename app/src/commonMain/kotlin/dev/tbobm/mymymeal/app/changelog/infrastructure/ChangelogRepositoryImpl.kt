package dev.tbobm.mymymeal.app.changelog.infrastructure

import dev.tbobm.mymymeal.app.changelog.domain.Changelog
import dev.tbobm.mymymeal.app.changelog.domain.ChangelogRepository
import dev.tbobm.mymymeal.app.common.config.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class ChangelogRepositoryImpl(private val appConfig: AppConfig) : ChangelogRepository {
    override fun observe(): Flow<Changelog> = flowOf(StaticChangelog(appConfig))
}
