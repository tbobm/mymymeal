package dev.tbobm.mymymeal.app.food.domain.usecase

import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.log.logAndReturnFailure
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.entity.Product
import dev.tbobm.mymymeal.app.food.domain.entity.Recipe
import dev.tbobm.mymymeal.app.food.domain.repository.ProductRepository
import dev.tbobm.mymymeal.app.food.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first

sealed interface DeleteFoodError {
    data object FoodNotFound : DeleteFoodError
}

class DeleteFoodUseCase(
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun delete(foodId: FoodId): Result<Unit, DeleteFoodError> =
        transactionProvider.withTransaction {
            val food =
                when (foodId) {
                    is FoodId.Product -> productRepository.observeProduct(foodId)
                    is FoodId.Recipe -> recipeRepository.observeRecipe(foodId)
                }.first()

            if (food == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    error = DeleteFoodError.FoodNotFound,
                    message = { "Food with ID $foodId not found." },
                )
            }

            when (food) {
                is Product -> productRepository.deleteProduct(food)
                is Recipe -> recipeRepository.deleteRecipe(food)
            }

            Ok(Unit)
        }

    private companion object {
        const val TAG = "DeleteFoodUseCase"
    }
}
