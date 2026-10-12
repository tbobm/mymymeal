package dev.tbobm.mymymeal.app.app.ui.food.diary.add

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId

@Immutable
internal sealed interface FoodModel {
    val foodId: FoodId
    val name: String
    val nutritionFacts: NutritionFacts
    val isLiquid: Boolean
    val note: String?
    val totalWeight: Double?
    val servingWeight: Double?

    val canUnpack: Boolean
        get() = foodId is FoodId.Recipe

    fun weight(measurement: Measurement): Double
}
