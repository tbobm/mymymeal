package dev.tbobm.mymymeal.app.app.ui.food.diary.search

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.Meal

@Immutable
internal data class MealModel(val id: Long, val name: String) {
    constructor(meal: Meal) : this(id = meal.id, name = meal.name)
}
