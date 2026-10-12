package dev.tbobm.mymymeal.app.fooddiary.domain.entity

import dev.tbobm.mymymeal.app.common.domain.food.FoodSource
import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.domain.food.WeightCalculator
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement

data class DiaryFoodProduct(
    override val name: String,
    override val nutritionFacts: NutritionFacts,
    override val servingWeight: Double?,
    override val totalWeight: Double?,
    override val isLiquid: Boolean,
    val source: FoodSource,
    override val note: String?,
) : DiaryFood {
    override fun weight(measurement: Measurement): Double =
        WeightCalculator.calculateWeight(
            measurement = measurement,
            servingWeight = servingWeight,
            totalWeight = totalWeight,
        ) ?: error("Cannot calculate weight for $this with measurement $measurement")
}
