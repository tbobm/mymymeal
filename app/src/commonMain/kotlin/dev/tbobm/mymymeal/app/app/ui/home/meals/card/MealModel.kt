package dev.tbobm.mymymeal.app.app.ui.home.meals.card

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.FoodDiaryEntryId
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.ManualDiaryEntryId
import kotlinx.datetime.LocalTime

@Immutable
internal data class MealModel(
    val id: Long,
    val name: String,
    val from: LocalTime,
    val to: LocalTime,
    val isAllDay: Boolean,
    val foods: List<MealEntryModel>,
    val energy: Int,
    val proteins: Double,
    val carbohydrates: Double,
    val fats: Double,
)

@Immutable
internal sealed interface MealEntryModel {
    val name: String
    val energy: Int?
    val proteins: Double?
    val carbohydrates: Double?
    val fats: Double?
}

@Immutable
internal data class FoodMealEntryModel(
    val id: FoodDiaryEntryId,
    override val name: String,
    override val energy: Int?,
    override val proteins: Double?,
    override val carbohydrates: Double?,
    override val fats: Double?,
    val measurement: Measurement,
    val weight: Double?,
    val isLiquid: Boolean,
    val isRecipe: Boolean,
    val servingWeight: Double?,
    val totalWeight: Double?,
) : MealEntryModel

@Immutable
internal data class ManualMealEntryModel(
    val id: ManualDiaryEntryId,
    override val name: String,
    override val energy: Int?,
    override val proteins: Double?,
    override val carbohydrates: Double?,
    override val fats: Double?,
) : MealEntryModel
