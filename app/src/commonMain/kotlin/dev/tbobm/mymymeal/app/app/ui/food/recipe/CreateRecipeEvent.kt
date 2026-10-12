package dev.tbobm.mymymeal.app.app.ui.food.recipe

import dev.tbobm.mymymeal.app.food.domain.entity.FoodId

internal sealed interface CreateRecipeEvent {
    data class Created(val recipeId: FoodId.Recipe) : CreateRecipeEvent
}
