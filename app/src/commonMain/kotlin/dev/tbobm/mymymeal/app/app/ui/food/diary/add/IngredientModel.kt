package dev.tbobm.mymymeal.app.app.ui.food.diary.add

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.entity.Recipe
import dev.tbobm.mymymeal.app.food.domain.entity.RecipeIngredient

@Immutable
internal data class IngredientModel(
    val foodId: FoodId,
    val name: String,
    val nutritionFacts: NutritionFacts?,
    val measurement: Measurement,
    val isRecipe: Boolean,
    val totalWeight: Double?,
    val servingWeight: Double?,
    val isLiquid: Boolean,
) {
    val isValid: Boolean
        get() =
            when (measurement) {
                is Measurement.FluidOunce,
                is Measurement.Gram,
                is Measurement.Milliliter,
                is Measurement.Ounce -> true

                is Measurement.Package -> totalWeight != null
                is Measurement.Serving -> servingWeight != null
            }

    constructor(
        ingredient: RecipeIngredient
    ) : this(
        foodId = ingredient.food.id,
        name = ingredient.food.headline,
        nutritionFacts = ingredient.nutritionFacts,
        measurement = ingredient.measurement,
        isRecipe = ingredient.food is Recipe,
        totalWeight = ingredient.food.totalWeight,
        servingWeight = ingredient.food.servingWeight,
        isLiquid = ingredient.food.isLiquid,
    )
}
