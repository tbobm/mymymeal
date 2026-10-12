package dev.tbobm.mymymeal.app.app.ui.meal

import androidx.compose.runtime.*
import kotlinx.datetime.LocalTime

@Immutable
internal data class MealModel(
    val id: Long,
    val name: String,
    val from: LocalTime,
    val to: LocalTime,
    val isAllDay: Boolean,
)
