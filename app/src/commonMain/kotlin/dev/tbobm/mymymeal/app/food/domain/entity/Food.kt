package dev.tbobm.mymymeal.app.food.domain.entity

import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.domain.food.WeightCalculator
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement

sealed interface Food {
    val id: FoodId
    val headline: String
    val totalWeight: Double?
    val servingWeight: Double?
    val nutritionFacts: NutritionFacts
    val isLiquid: Boolean

    fun weight(measurement: Measurement): Double? =
        WeightCalculator.calculateWeight(
            measurement = measurement,
            totalWeight = totalWeight,
            servingWeight = servingWeight,
        )
}
