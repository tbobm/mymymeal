package dev.tbobm.mymymeal.app.food.domain.usecase

import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.log.logAndReturnFailure
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.food.domain.entity.FoodHistory
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.entity.Recipe
import dev.tbobm.mymymeal.app.food.domain.entity.RecipeIngredient
import dev.tbobm.mymymeal.app.food.domain.repository.FoodHistoryRepository
import dev.tbobm.mymymeal.app.food.domain.repository.ProductRepository
import dev.tbobm.mymymeal.app.food.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.firstOrNull

sealed interface CreateRecipeError {
    data object EmptyName : CreateRecipeError

    data object NonPositiveServings : CreateRecipeError

    data object EmptyIngredients : CreateRecipeError

    data class IngredientNotFound(val foodId: FoodId) : CreateRecipeError

    data object CircularIngredient : CreateRecipeError
}

class CreateRecipeUseCase(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository,
    private val historyRepository: FoodHistoryRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun create(
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<Pair<FoodId, Measurement>>,
        history: FoodHistory.CreationHistory,
    ): Result<FoodId.Recipe, CreateRecipeError> {
        if (name.isBlank()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = CreateRecipeError.EmptyName,
                message = { "Recipe name cannot be empty." },
            )
        }

        if (servings <= 0) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = CreateRecipeError.NonPositiveServings,
                message = { "Recipe servings must be a positive integer." },
            )
        }

        if (ingredients.isEmpty()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = CreateRecipeError.EmptyIngredients,
                message = { "Recipe must have at least one ingredient." },
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
                            error = CreateRecipeError.IngredientNotFound(foodId),
                            message = { "Ingredient with ID $foodId not found." },
                        )
                    }

                    RecipeIngredient(food, measurement)
                }

            val recipe =
                Recipe(
                    id = FoodId.Recipe(0L),
                    name = name,
                    servings = servings,
                    note = note?.takeIf { it.isNotBlank() },
                    isLiquid = isLiquid,
                    ingredients = ingredients,
                )

            // Check for circular references in ingredients
            val flatIngredients = recipe.flatIngredients()
            val ingredientIds = flatIngredients.map { it.id }.toSet()
            if (flatIngredients.size != ingredientIds.size) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    error = CreateRecipeError.CircularIngredient,
                    message = { "Recipe contains circular ingredient references." },
                )
            }

            val recipeId =
                recipeRepository.insertRecipe(
                    name = name,
                    servings = servings,
                    note = note?.takeIf { it.isNotBlank() },
                    isLiquid = isLiquid,
                    ingredients = ingredients,
                )

            historyRepository.insert(recipeId, history)

            Ok(recipeId)
        }
    }

    private companion object {
        const val TAG = "CreateRecipeUseCase"
    }
}
