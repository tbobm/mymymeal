package dev.tbobm.mymymeal.app.food.domain.usecase

import dev.tbobm.mymymeal.app.food.domain.entity.Food
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.repository.ProductRepository
import dev.tbobm.mymymeal.app.food.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class ObserveFoodUseCase(
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
) {
    fun observe(foodId: FoodId): Flow<Food?> =
        when (foodId) {
            is FoodId.Product -> productRepository.observeProduct(foodId)
            is FoodId.Recipe -> recipeRepository.observeRecipe(foodId)
        }
}
