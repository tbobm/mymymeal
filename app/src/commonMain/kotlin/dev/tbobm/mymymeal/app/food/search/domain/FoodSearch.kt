package dev.tbobm.mymymeal.app.food.search.domain

import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.domain.food.WeightCalculator
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId

sealed interface FoodSearch {
    val id: FoodId
    val headline: String
    val isLiquid: Boolean
    val suggestedMeasurement: Measurement

    data class Product(
        override val id: FoodId.Product,
        override val headline: String,
        override val isLiquid: Boolean,
        val nutritionFacts: NutritionFacts,
        val totalWeight: Double?,
        val servingWeight: Double?,
        override val suggestedMeasurement: Measurement,
    ) : FoodSearch {
        fun weight(measurement: Measurement): Double? =
            WeightCalculator.calculateWeight(
                measurement = measurement,
                totalWeight = totalWeight,
                servingWeight = servingWeight,
            )
    }

    data class Recipe(
        override val id: FoodId.Recipe,
        override val headline: String,
        override val isLiquid: Boolean,
        override val suggestedMeasurement: Measurement,
    ) : FoodSearch
}
