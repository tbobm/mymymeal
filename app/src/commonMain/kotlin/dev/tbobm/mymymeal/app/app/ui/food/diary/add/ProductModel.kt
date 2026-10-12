package dev.tbobm.mymymeal.app.app.ui.food.diary.add

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.common.domain.food.FoodSource
import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.domain.food.WeightCalculator
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.entity.Product

@Immutable
internal data class ProductModel(
    override val foodId: FoodId.Product,
    override val name: String,
    override val nutritionFacts: NutritionFacts,
    override val isLiquid: Boolean,
    override val note: String?,
    val source: FoodSource,
    override val totalWeight: Double?,
    override val servingWeight: Double?,
) : FoodModel {
    constructor(
        product: Product
    ) : this(
        foodId = product.id,
        name = product.headline,
        nutritionFacts = product.nutritionFacts,
        isLiquid = product.isLiquid,
        note = product.note,
        source = product.source,
        totalWeight = product.totalWeight,
        servingWeight = product.servingWeight,
    )

    override fun weight(measurement: Measurement): Double =
        WeightCalculator.calculateWeight(
            measurement = measurement,
            servingWeight = servingWeight,
            totalWeight = totalWeight,
        ) ?: error("Weight cannot be calculated")
}
