package dev.tbobm.mymymeal.app.food.domain.usecase

import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.log.logAndReturnFailure
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.entity.RecipeIngredient
import dev.tbobm.mymymeal.app.food.domain.repository.ProductRepository
import dev.tbobm.mymymeal.app.food.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.firstOrNull

sealed interface UpdateRecipeError {
    data object EmptyName : UpdateRecipeError

    data object NonPositiveServings : UpdateRecipeError

    data object EmptyIngredients : UpdateRecipeError

    data class RecipeNotFound(val id: FoodId.Recipe) : UpdateRecipeError

    data class IngredientNotFound(val foodId: FoodId) : UpdateRecipeError

    data object CircularIngredient : UpdateRecipeError
}

class UpdateRecipeUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun update(
        id: FoodId.Recipe,
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<Pair<FoodId, Measurement>>,
    ): Result<Unit, UpdateRecipeError> {
        if (name.isBlank()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = UpdateRecipeError.EmptyName,
                message = { "Recipe name cannot be empty." },
            )
        }

        if (servings <= 0) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = UpdateRecipeError.NonPositiveServings,
                message = { "Recipe servings must be a positive integer." },
            )
        }

        if (ingredients.isEmpty()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = UpdateRecipeError.EmptyIngredients,
                message = { "Recipe must have at least one ingredient." },
            )
        }

        val recipe = recipeRepository.observeRecipe(id).firstOrNull()

        if (recipe == null) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = UpdateRecipeError.RecipeNotFound(id),
                message = { "Recipe with ID $id not found." },
            )
        }

        return transactionProvider.withTransaction {
            val ingredients =
                ingredients.map { (foodId, measurement) ->
                    val food =
                        when (foodId) {
                            is FoodId.Product ->
                                productRepository.observeProduct(foodId).firstOrNull()

                            is FoodId.Recipe -> recipeRepository.observeRecipe(foodId).firstOrNull()
                        }

                    if (food == null) {
                        return@withTransaction logger.logAndReturnFailure(
                            tag = TAG,
                            throwable = null,
                            error = UpdateRecipeError.IngredientNotFound(foodId),
                            message = { "Ingredient with ID $foodId not found." },
                        )
                    }

                    RecipeIngredient(food, measurement)
                }

            val updatedRecipe =
                recipe.copy(
                    name = name,
                    servings = servings,
                    note = note,
                    isLiquid = isLiquid,
                    ingredients = ingredients,
                )

            // Check for circular references in ingredients
            val flatIngredients = recipe.flatIngredients()
            val ingredientIds = flatIngredients.map { it.id }.toSet()
            if (flatIngredients.size != ingredientIds.size) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    throwable = null,
                    error = UpdateRecipeError.CircularIngredient,
                    message = { "Recipe contains circular ingredient references." },
                )
            }

            recipeRepository.updateRecipe(updatedRecipe)
            Ok(Unit)
        }
    }

    private companion object {
        const val TAG = "UpdateRecipeUseCase"
    }
}
