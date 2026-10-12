package dev.tbobm.mymymeal.app.importexport.domain.usecase

import dev.tbobm.mymymeal.app.common.domain.measurement.rawValue
import dev.tbobm.mymymeal.app.common.domain.measurement.type
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.entity.Recipe
import dev.tbobm.mymymeal.app.food.domain.entity.RecipeIngredient
import dev.tbobm.mymymeal.app.food.domain.repository.RecipeRepository
import dev.tbobm.mymymeal.app.importexport.domain.entity.RecipeIngredientField
import dev.tbobm.mymymeal.app.importexport.domain.entity.csvHeader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first

fun interface ExportRecipeIngredientsUseCase {
    /** Exports one row per recipe ingredient, referencing its owning recipe by id and name. */
    suspend fun export(): Flow<String>
}

internal class ExportRecipeIngredientsUseCaseImpl(private val recipeRepository: RecipeRepository) :
    ExportRecipeIngredientsUseCase {
    override suspend fun export(): Flow<String> = channelFlow {
        val csvWriter = CsvWriter()

        val header =
            RecipeIngredientField.entries
                .map(RecipeIngredientField::csvHeader)
                .joinToString(",", transform = csvWriter::writeString)
        send(header)

        val recipes = recipeRepository.observeAllRecipes().first()
        for (recipe in recipes) {
            for (ingredient in recipe.ingredients) {
                val csvLine =
                    RecipeIngredientField.entries.joinToString(separator = ",") { field ->
                        csvWriter.write(ingredient.field(recipe, field))
                    }
                send(csvLine)
            }
        }
    }
}

private fun RecipeIngredient.field(recipe: Recipe, field: RecipeIngredientField): Any? =
    when (field) {
        RecipeIngredientField.RecipeId -> recipe.id.id
        RecipeIngredientField.RecipeName -> recipe.name
        RecipeIngredientField.IngredientType ->
            when (food.id) {
                is FoodId.Product -> "product"
                is FoodId.Recipe -> "recipe"
            }

        RecipeIngredientField.IngredientName -> food.headline
        RecipeIngredientField.MeasurementType -> measurement.type.name
        RecipeIngredientField.MeasurementValue -> measurement.rawValue
    }
