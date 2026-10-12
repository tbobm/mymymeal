package dev.tbobm.mymymeal.app.food.domain

import dev.tbobm.mymymeal.app.common.infrastructure.koin.eventHandlerOf
import dev.tbobm.mymymeal.app.food.domain.event.FoodDiaryEntryCreatedEventHandler
import dev.tbobm.mymymeal.app.food.domain.usecase.CreateProductUseCase
import dev.tbobm.mymymeal.app.food.domain.usecase.CreateRecipeUseCase
import dev.tbobm.mymymeal.app.food.domain.usecase.DeleteFoodUseCase
import dev.tbobm.mymymeal.app.food.domain.usecase.DownloadProductUseCase
import dev.tbobm.mymymeal.app.food.domain.usecase.ObserveFoodUseCase
import dev.tbobm.mymymeal.app.food.domain.usecase.ObserveMeasurementSuggestionsUseCase
import dev.tbobm.mymymeal.app.food.domain.usecase.UpdateProductUseCase
import dev.tbobm.mymymeal.app.food.domain.usecase.UpdateRecipeUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf

fun Module.foodDomainModule() {
    factoryOf(::CreateProductUseCase)
    factoryOf(::CreateRecipeUseCase)
    factoryOf(::DeleteFoodUseCase)
    factoryOf(::DownloadProductUseCase)
    factoryOf(::ObserveFoodUseCase)
    factoryOf(::ObserveMeasurementSuggestionsUseCase)
    factoryOf(::UpdateProductUseCase)
    factoryOf(::UpdateRecipeUseCase)

    eventHandlerOf(::FoodDiaryEntryCreatedEventHandler)
}
