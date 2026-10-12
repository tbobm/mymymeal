package dev.tbobm.mymymeal.app.food.infrastructure

import dev.tbobm.mymymeal.app.food.domain.repository.FoodHistoryRepository
import dev.tbobm.mymymeal.app.food.domain.repository.FoodMeasurementSuggestionRepository
import dev.tbobm.mymymeal.app.food.domain.repository.ProductRepository
import dev.tbobm.mymymeal.app.food.domain.repository.RecipeRepository
import dev.tbobm.mymymeal.app.food.domain.repository.RemoteProductRequestFactory
import dev.tbobm.mymymeal.app.food.infrastructure.network.RemoteProductMapper
import dev.tbobm.mymymeal.app.food.infrastructure.network.RemoteProductRequestFactoryImpl
import dev.tbobm.mymymeal.app.food.infrastructure.openfoodfacts.openFoodFactsModule
import dev.tbobm.mymymeal.app.food.infrastructure.repository.RoomFoodHistoryRepository
import dev.tbobm.mymymeal.app.food.infrastructure.repository.RoomFoodMeasurementSuggestionRepository
import dev.tbobm.mymymeal.app.food.infrastructure.repository.RoomProductRepository
import dev.tbobm.mymymeal.app.food.infrastructure.repository.RoomRecipeRepository
import dev.tbobm.mymymeal.app.food.infrastructure.room.FoodDatabase
import dev.tbobm.mymymeal.app.food.infrastructure.usda.USDAModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind

fun Module.foodInfrastructureModule() {
    factory { database.foodEventDao }
    factory { database.measurementSuggestionDao }
    factory { database.productDao }
    factory { database.recipeDao }

    factoryOf(::RoomFoodHistoryRepository).bind<FoodHistoryRepository>()
    factoryOf(::RoomFoodMeasurementSuggestionRepository).bind<FoodMeasurementSuggestionRepository>()
    factoryOf(::RoomProductRepository).bind<ProductRepository>()
    factoryOf(::RoomRecipeRepository).bind<RecipeRepository>()

    factoryOf(::RemoteProductRequestFactoryImpl).bind<RemoteProductRequestFactory>()
    factoryOf(::RemoteProductMapper)

    USDAModule()
    openFoodFactsModule()
}

private val Scope.database: FoodDatabase
    get() = get()
