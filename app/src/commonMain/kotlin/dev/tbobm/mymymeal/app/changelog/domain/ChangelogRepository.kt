package dev.tbobm.mymymeal.app.changelog.domain

import kotlinx.coroutines.flow.Flow

interface ChangelogRepository {
    fun observe(): Flow<Changelog>
}
