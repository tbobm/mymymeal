package com.maksimowiczm.foodyou.app.ui.goals.master

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.goals.domain.entity.DailyGoal
import com.maksimowiczm.foodyou.goals.domain.entity.RollingBudgetPreferences
import com.maksimowiczm.foodyou.goals.domain.usecase.RollingEnergyBalance

@Immutable
internal data class GoalsScreenUiState(
    val meals: List<MealModel>,
    val goal: DailyGoal,
    val rollingBalance: RollingEnergyBalance,
    val rollingBudgetPreferences: RollingBudgetPreferences,
    /** Caffeine mg logged via the Habits coffee tracker (not food) -- added to the diary total. */
    val habitsCaffeineMg: Int,
)
