package dev.tbobm.mymymeal.app.food.infrastructure.repository

import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.common.domain.measurement.from
import dev.tbobm.mymymeal.app.common.domain.measurement.rawValue
import dev.tbobm.mymymeal.app.common.domain.measurement.type
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.entity.Recipe
import dev.tbobm.mymymeal.app.food.domain.entity.RecipeIngredient
import dev.tbobm.mymymeal.app.food.domain.repository.RecipeRepository
import dev.tbobm.mymymeal.app.food.infrastructure.room.RecipeDao
import dev.tbobm.mymymeal.app.food.infrastructure.room.RecipeEntity
import dev.tbobm.mymymeal.app.food.infrastructure.room.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class RoomRecipeRepository(
    private val recipeDao: RecipeDao,
    private val productDataSource: RoomProductRepository,
) : RecipeRepository {
    override fun observeRecipe(recipeId: FoodId.Recipe): Flow<Recipe?> =
        combine(
                recipeDao.observeRecipe(recipeId.id),
                recipeDao.observeRecipeIngredients(recipeId.id),
            ) { recipeEntity, ingredients ->
                if (recipeEntity == null) {
                    return@combine null
                }

                val ingredients =
                    ingredients.map {
                        val foodId = it.foodId

                        val foodFlow =
                            when (foodId) {
                                is FoodId.Recipe -> observeRecipe(foodId).filterNotNull()
                                is FoodId.Product ->
                                    productDataSource.observeProduct(foodId).filterNotNull()
                            }

                        foodFlow.map { food ->
                            RecipeIngredient(
                                food = food,
                                measurement = Measurement.from(it.measurement, it.quantity),
                            )
                        }
                    }

                recipeEntity to combine(ingredients) { it.toList() }
            }
            .flatMapLatest { pair ->
                if (pair == null) {
                    return@flatMapLatest flowOf(null)
                } else {
                    val (recipeEntity, ingredients) = pair

                    ingredients.map { ingredients ->
                        Recipe(
                            id = FoodId.Recipe(recipeEntity.id),
                            name = recipeEntity.name,
                            servings = recipeEntity.servings,
                            note = recipeEntity.note,
                            isLiquid = recipeEntity.isLiquid,
                            ingredients = ingredients,
                        )
                    }
                }
            }

    override fun observeAllRecipes(): Flow<List<Recipe>> =
        recipeDao.observeAllRecipes().flatMapLatest { entities ->
            if (entities.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(entities.map { observeRecipe(FoodId.Recipe(it.id)).filterNotNull() }) {
                    it.toList()
                }
            }
        }

    override suspend fun deleteRecipe(recipe: Recipe) {
        val entity = recipe.toEntity()
        recipeDao.delete(entity)
    }

    override suspend fun insertRecipe(
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<RecipeIngredient>,
    ): FoodId.Recipe {
        val recipe =
            Recipe(
                id = FoodId.Recipe(0L),
                name = name,
                servings = servings,
                note = note,
                isLiquid = isLiquid,
                ingredients = ingredients,
            )

        val recipeEntity = recipe.toEntity()

        val ingredients =
            recipe.ingredients.map { (food, measurement) ->
                val foodId = food.id

                RecipeIngredientEntity(
                    ingredientRecipeId = (foodId as? FoodId.Recipe)?.id,
                    ingredientProductId = (foodId as? FoodId.Product)?.id,
                    measurement = measurement.type,
                    quantity = measurement.rawValue,
                )
            }

        val id =
            recipeDao
                .insertRecipeWithIngredients(recipe = recipeEntity, ingredients = ingredients)
                .let(FoodId::Recipe)

        return id
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val recipeEntity = recipe.toEntity()

        val ingredients =
            recipe.ingredients.map { (food, measurement) ->
                val foodId = food.id

                RecipeIngredientEntity(
                    ingredientRecipeId = (foodId as? FoodId.Recipe)?.id,
                    ingredientProductId = (foodId as? FoodId.Product)?.id,
                    measurement = measurement.type,
                    quantity = measurement.rawValue,
                )
            }

        recipeDao.updateRecipeWithIngredients(recipeEntity, ingredients)
    }
}

private fun Recipe.toEntity(): RecipeEntity =
    RecipeEntity(
        id = this.id.id,
        name = this.name,
        servings = this.servings,
        note = this.note,
        isLiquid = this.isLiquid,
    )

private val RecipeIngredientEntity.foodId: FoodId
    get() =
        ingredientRecipeId?.let { FoodId.Recipe(it) }
            ?: ingredientProductId?.let { FoodId.Product(it) }
            ?: error(
                "RecipeIngredientEntity must have either ingredientRecipeId or ingredientProductId set"
            )
