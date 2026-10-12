package dev.tbobm.mymymeal.app.app.ui.food.recipe

internal sealed interface UpdateRecipeEvent {
    data object Updated : UpdateRecipeEvent
}
