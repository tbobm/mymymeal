package dev.tbobm.mymymeal.app.food.domain.repository

import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.entity.Recipe
import dev.tbobm.mymymeal.app.food.domain.entity.RecipeIngredient
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeRecipe(recipeId: FoodId.Recipe): Flow<Recipe?>

    /** Bulk read of every recipe. For full-data export. */
    fun observeAllRecipes(): Flow<List<Recipe>>

    suspend fun insertRecipe(
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<RecipeIngredient>,
    ): FoodId.Recipe

    suspend fun updateRecipe(recipe: Recipe)

    suspend fun deleteRecipe(recipe: Recipe)
}
