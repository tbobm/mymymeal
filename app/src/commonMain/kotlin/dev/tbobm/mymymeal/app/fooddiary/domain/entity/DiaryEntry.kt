package dev.tbobm.mymymeal.app.fooddiary.domain.entity

import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

sealed interface DiaryEntry {
    val mealId: Long
    val date: LocalDate
    val name: String
    val nutritionFacts: NutritionFacts
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
}
