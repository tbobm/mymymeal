package dev.tbobm.mymymeal.app.app.ui.food.recipe

import androidx.lifecycle.ViewModel
import dev.tbobm.mymymeal.app.common.extension.combine
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.entity.Recipe
import dev.tbobm.mymymeal.app.food.domain.entity.RecipeIngredient
import dev.tbobm.mymymeal.app.food.domain.usecase.ObserveFoodUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal abstract class RecipeViewModel(private val observeFoodUseCase: ObserveFoodUseCase) :
    ViewModel() {

    fun intoRecipe(state: RecipeFormState): Flow<Recipe?> {
        if (state.ingredients.isEmpty()) {
            return flowOf(null)
        }

        return state.ingredients
            .map { ingredient ->
                observeFoodUseCase.observe(ingredient.foodId).filterNotNull().map { food ->
                    RecipeIngredient(food = food, measurement = ingredient.measurement)
                }
            }
            .combine()
            .map { ingredients ->
                Recipe(
                    id = FoodId.Recipe(-1),
                    name = state.name.value,
                    servings = state.servings.value,
                    note = state.note.value,
                    ingredients = ingredients,
                    isLiquid = state.isLiquid,
                )
            }
    }
}
