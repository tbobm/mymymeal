package dev.tbobm.mymymeal.app.changelog

import dev.tbobm.mymymeal.app.changelog.domain.ChangelogRepository
import dev.tbobm.mymymeal.app.changelog.infrastructure.ChangelogRepositoryImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val changelogModule = module { factoryOf(::ChangelogRepositoryImpl).bind<ChangelogRepository>() }
