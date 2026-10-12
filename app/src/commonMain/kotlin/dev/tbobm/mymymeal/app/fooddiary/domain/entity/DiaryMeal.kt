package dev.tbobm.mymymeal.app.fooddiary.domain.entity

import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.domain.food.sum

data class DiaryMeal(val meal: Meal, val entries: List<DiaryEntry>) {
    val nutritionFacts: NutritionFacts = entries.map { it.nutritionFacts }.sum()
}
