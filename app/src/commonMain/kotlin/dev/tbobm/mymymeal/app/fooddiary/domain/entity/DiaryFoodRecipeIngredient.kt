package dev.tbobm.mymymeal.app.fooddiary.domain.entity

import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement

/**
 * Represents an ingredient in a food recipe within the food diary.
 *
 * @param food The food item that is part of the recipe.
 * @param measurement The measurement of the food item used in the recipe.
 */
data class DiaryFoodRecipeIngredient(val food: DiaryFood, val measurement: Measurement) {

    /** The weight of the ingredient based on the measurement provided. */
    val weight: Double = food.weight(measurement)

    /**
     * Total nutrition facts for the ingredient based on the food's nutrition facts and the weight
     * of the measurement.
     */
    val nutritionFacts: NutritionFacts = weight.div(100).let { food.nutritionFacts * it }
}
