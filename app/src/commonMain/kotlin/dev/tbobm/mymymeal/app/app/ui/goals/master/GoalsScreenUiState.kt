package dev.tbobm.mymymeal.app.app.ui.goals.master

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.goals.domain.entity.DailyGoal
import dev.tbobm.mymymeal.app.goals.domain.entity.RollingBudgetPreferences
import dev.tbobm.mymymeal.app.goals.domain.usecase.RollingEnergyBalance

@Immutable
internal data class GoalsScreenUiState(
    val meals: List<MealModel>,
    val goal: DailyGoal,
    val rollingBalance: RollingEnergyBalance,
    val rollingBudgetPreferences: RollingBudgetPreferences,
    /** Caffeine mg logged via the Habits coffee tracker (not food) -- added to the diary total. */
    val habitsCaffeineMg: Int,
)
