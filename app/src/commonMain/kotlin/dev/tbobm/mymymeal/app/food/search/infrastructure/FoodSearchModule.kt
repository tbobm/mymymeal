package dev.tbobm.mymymeal.app.food.search.infrastructure

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepositoryOf
import dev.tbobm.mymymeal.app.food.domain.repository.FoodSearchHistoryRepository
import dev.tbobm.mymymeal.app.food.search.domain.FoodRemoteMediatorFactoryAggregate
import dev.tbobm.mymymeal.app.food.search.domain.FoodSearchRepository
import dev.tbobm.mymymeal.app.food.search.domain.ProductRemoteMediatorFactory
import dev.tbobm.mymymeal.app.food.search.infrastructure.openfoodfacts.OpenFoodFactsRemoteMediatorFactory
import dev.tbobm.mymymeal.app.food.search.infrastructure.repository.DataStoreFoodSearchPreferencesRepository
import dev.tbobm.mymymeal.app.food.search.infrastructure.repository.RoomFoodSearchHistoryRepository
import dev.tbobm.mymymeal.app.food.search.infrastructure.repository.RoomFoodSearchRepository
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.FoodSearchDatabase
import dev.tbobm.mymymeal.app.food.search.infrastructure.usda.USDARemoteMediatorFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind

fun Module.foodSearchInfrastructureModule() {
    factoryOf(::FoodRemoteMediatorFactoryAggregateImpl).bind<FoodRemoteMediatorFactoryAggregate>()

    factoryOf(::RoomFoodSearchHistoryRepository).bind<FoodSearchHistoryRepository>()
    factoryOf(::RoomFoodSearchRepository).bind<FoodSearchRepository>()

    userPreferencesRepositoryOf(::DataStoreFoodSearchPreferencesRepository)

    factory { database.foodSearchDao }
    factory { database.usdaPagingKeyDao }
    factory { database.openFoodFactsPagingKeyDao }

    factoryOf(::OpenFoodFactsRemoteMediatorFactory).bind<ProductRemoteMediatorFactory>()
    factory {
            USDARemoteMediatorFactory(
                foodSearchPreferencesRepository = userPreferencesRepository(),
                transactionProvider = get(),
                productRepository = get(),
                historyRepository = get(),
                remoteDataSource = get(),
                pagingKeyDao = get(),
                usdaMapper = get(),
                remoteMapper = get(),
                dateProvider = get(),
                logger = get(),
            )
        }
        .bind<ProductRemoteMediatorFactory>()
}

private val Scope.database: FoodSearchDatabase
    get() = get()
