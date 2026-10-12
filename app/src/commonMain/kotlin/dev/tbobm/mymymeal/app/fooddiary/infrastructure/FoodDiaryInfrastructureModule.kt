package dev.tbobm.mymymeal.app.fooddiary.infrastructure

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepositoryOf
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.FoodDiaryEntryRepository
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.ManualDiaryEntryRepository
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.MealRepository
import dev.tbobm.mymymeal.app.fooddiary.domain.service.LocalizedMealsProvider
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.compose.ComposeLocalizedMealsProvider
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.healthexport.DataStoreHealthConnectPreferencesRepository
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.repository.DataStoreMealsPreferencesRepository
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.repository.RoomFoodDiaryEntryRepository
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.repository.RoomManualDiaryEntryRepository
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.repository.RoomMealRepository
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.FoodDiaryDatabase
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.InitializeMealsCallback
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind

internal fun Module.foodDiaryInfrastructureModule() {
    factoryOf(::ComposeLocalizedMealsProvider).bind<LocalizedMealsProvider>()

    userPreferencesRepositoryOf(::DataStoreMealsPreferencesRepository)
    userPreferencesRepositoryOf(::DataStoreHealthConnectPreferencesRepository)
    factoryOf(::RoomFoodDiaryEntryRepository).bind<FoodDiaryEntryRepository>()
    factoryOf(::RoomManualDiaryEntryRepository).bind<ManualDiaryEntryRepository>()
    factoryOf(::RoomMealRepository).bind<MealRepository>()

    factoryOf(::InitializeMealsCallback)

    factory { database.mealDao }
    factory { database.manualDiaryEntryDao }
    factory { database.measurementDao }
}

private val Scope.database: FoodDiaryDatabase
    get() = get()
