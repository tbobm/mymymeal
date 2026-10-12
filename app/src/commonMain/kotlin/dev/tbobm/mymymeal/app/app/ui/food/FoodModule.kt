package dev.tbobm.mymymeal.app.app.ui.food

import dev.tbobm.mymymeal.app.app.ui.food.product.foodProduct
import dev.tbobm.mymymeal.app.app.ui.food.recipe.foodRecipe
import dev.tbobm.mymymeal.app.app.ui.food.search.FoodSearchViewModel
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.food() {
    viewModel { (excluded: FoodId.Recipe?) ->
        FoodSearchViewModel(
            excludedRecipeId = excluded,
            foodSearchPreferencesRepository = userPreferencesRepository(),
            searchHistoryRepository = get(),
            foodSearchRepository = get(),
            foodSearchUseCase = get(),
            tagRepository = get(),
            dateProvider = get(),
        )
    }

    foodProduct()
    foodRecipe()
}
