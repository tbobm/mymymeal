package dev.tbobm.mymymeal.app.food.domain.entity

import dev.tbobm.mymymeal.app.common.domain.food.FoodSource

data class RemoteProduct(
    val name: String?,
    val brand: String?,
    val barcode: String?,
    val nutritionFacts: RemoteNutritionFacts?,
    val packageWeight: Double?,
    val servingWeight: Double?,
    val source: FoodSource,
    val isLiquid: Boolean,
)
