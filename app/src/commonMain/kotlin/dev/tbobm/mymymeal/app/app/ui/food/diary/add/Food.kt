package dev.tbobm.mymymeal.app.app.ui.food.diary.add

import dev.tbobm.mymymeal.app.food.domain.entity.Food
import dev.tbobm.mymymeal.app.food.domain.entity.Product
import dev.tbobm.mymymeal.app.food.domain.entity.Recipe
import dev.tbobm.mymymeal.app.food.domain.entity.RecipeIngredient
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.DiaryFood
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.DiaryFoodProduct
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.DiaryFoodRecipe
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.DiaryFoodRecipeIngredient

internal fun Food.toDiaryFood(): DiaryFood =
    when (this) {
        is Product -> toDiaryProduct()
        is Recipe -> toDiaryRecipe()
    }

private fun Product.toDiaryProduct(): DiaryFoodProduct =
    DiaryFoodProduct(
        name = headline,
        nutritionFacts = nutritionFacts,
        servingWeight = servingWeight,
        totalWeight = totalWeight,
        isLiquid = isLiquid,
        source = source,
        note = note,
    )

private fun Recipe.toDiaryRecipe(): DiaryFoodRecipe =
    DiaryFoodRecipe(
        name = headline,
        servings = servings,
        ingredients = ingredients.map { it.toDiaryRecipeIngredient() },
        isLiquid = isLiquid,
        note = note,
    )

private fun RecipeIngredient.toDiaryRecipeIngredient(): DiaryFoodRecipeIngredient =
    DiaryFoodRecipeIngredient(food = food.toDiaryFood(), measurement = measurement)
