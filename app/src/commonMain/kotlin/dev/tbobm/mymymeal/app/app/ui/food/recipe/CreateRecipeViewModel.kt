package dev.tbobm.mymymeal.app.app.ui.food.recipe

import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.result.onError
import dev.tbobm.mymymeal.app.common.result.onSuccess
import dev.tbobm.mymymeal.app.food.domain.entity.FoodHistory
import dev.tbobm.mymymeal.app.food.domain.usecase.CreateRecipeUseCase
import dev.tbobm.mymymeal.app.food.domain.usecase.ObserveFoodUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class CreateRecipeViewModel(
    observeFoodUseCase: ObserveFoodUseCase,
    private val createRecipeUseCase: CreateRecipeUseCase,
    private val dateProvider: DateProvider,
) : RecipeViewModel(observeFoodUseCase) {

    private val eventBus = Channel<CreateRecipeEvent>()
    val events = eventBus.receiveAsFlow()

    fun create(form: RecipeFormState) {
        if (!form.isValid) {
            return
        }

        viewModelScope.launch {
            createRecipeUseCase
                .create(
                    name = form.name.value,
                    servings = form.servings.value,
                    note = form.note.value,
                    isLiquid = form.isLiquid,
                    ingredients = form.ingredients.map { it.intoPair() },
                    history = FoodHistory.Created(dateProvider.nowInstant()),
                )
                .onSuccess { eventBus.send(CreateRecipeEvent.Created(it)) }
                .onError {
                    // Explode
                    error("Failed to create recipe: $it")
                }
        }
    }
}
