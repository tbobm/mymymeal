package dev.tbobm.mymymeal.app.fooddiary.domain

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.fooddiary.domain.usecase.CreateFoodDiaryEntryUseCase
import dev.tbobm.mymymeal.app.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import dev.tbobm.mymymeal.app.fooddiary.domain.usecase.UnpackFoodDiaryEntryUseCase
import dev.tbobm.mymymeal.app.fooddiary.domain.usecase.UpdateFoodDiaryEntryUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf

internal fun Module.foodDiaryDomainModule() {
    factoryOf(::CreateFoodDiaryEntryUseCase)
    factoryOf(::UnpackFoodDiaryEntryUseCase)
    factoryOf(::UpdateFoodDiaryEntryUseCase)

    factory {
        ObserveDiaryMealsUseCase(
            mealRepository = get(),
            mealsPreferencesRepository = userPreferencesRepository(),
            foodEntryRepository = get(),
            manualEntryRepository = get(),
            dateProvider = get(),
        )
    }
}
