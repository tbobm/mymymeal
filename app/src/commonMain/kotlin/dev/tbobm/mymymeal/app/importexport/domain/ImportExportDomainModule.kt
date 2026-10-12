package dev.tbobm.mymymeal.app.importexport.domain

import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportCsvProductsUseCase
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportCsvProductsUseCaseImpl
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportDiaryEntriesUseCase
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportDiaryEntriesUseCaseImpl
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportRecipeIngredientsUseCase
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportRecipeIngredientsUseCaseImpl
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportRecipesUseCase
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportRecipesUseCaseImpl
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ImportCsvProductUseCase
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ImportCsvProductUseCaseImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind

internal fun Module.importExportDomainModule() {
    factoryOf(::ExportCsvProductsUseCaseImpl).bind<ExportCsvProductsUseCase>()
    factoryOf(::ImportCsvProductUseCaseImpl).bind<ImportCsvProductUseCase>()
    factoryOf(::ExportDiaryEntriesUseCaseImpl).bind<ExportDiaryEntriesUseCase>()
    factoryOf(::ExportRecipesUseCaseImpl).bind<ExportRecipesUseCase>()
    factoryOf(::ExportRecipeIngredientsUseCaseImpl).bind<ExportRecipeIngredientsUseCase>()
}
