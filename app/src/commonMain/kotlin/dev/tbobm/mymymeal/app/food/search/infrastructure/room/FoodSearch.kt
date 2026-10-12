package dev.tbobm.mymymeal.app.food.search.infrastructure.room

import androidx.room.Embedded
import dev.tbobm.mymymeal.app.common.domain.measurement.MeasurementType
import dev.tbobm.mymymeal.app.common.infrastructure.room.Minerals
import dev.tbobm.mymymeal.app.common.infrastructure.room.Nutrients
import dev.tbobm.mymymeal.app.common.infrastructure.room.Vitamins

data class FoodSearch(
    val productId: Long?,
    val recipeId: Long?,
    val headline: String,
    val isLiquid: Boolean,
    @Embedded val nutrients: Nutrients?,
    @Embedded val vitamins: Vitamins?,
    @Embedded val minerals: Minerals?,
    val totalWeight: Double?,
    val servingWeight: Double?,
    val measurementType: MeasurementType?,
    val measurementValue: Double?,
)
