package dev.tbobm.mymymeal.app.importexport.swissfoodcompositiondatabase

import dev.tbobm.mymymeal.app.importexport.swissfoodcompositiondatabase.domain.ImportSwissFoodCompositionDatabaseUseCase
import dev.tbobm.mymymeal.app.importexport.swissfoodcompositiondatabase.domain.ImportSwissFoodCompositionDatabaseUseCaseImpl
import dev.tbobm.mymymeal.app.importexport.swissfoodcompositiondatabase.domain.SwissFoodCompositionDatabaseRepository
import dev.tbobm.mymymeal.app.importexport.swissfoodcompositiondatabase.infrastructure.ComposeSwissFoodCompositionDatabaseRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val importExportSwissFoodCompositionDatabaseModule = module {
    factoryOf(::ImportSwissFoodCompositionDatabaseUseCaseImpl)
        .bind<ImportSwissFoodCompositionDatabaseUseCase>()

    factoryOf(::ComposeSwissFoodCompositionDatabaseRepository)
        .bind<SwissFoodCompositionDatabaseRepository>()
}
