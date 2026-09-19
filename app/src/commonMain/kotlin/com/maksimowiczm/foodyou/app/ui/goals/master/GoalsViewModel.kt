package com.maksimowiczm.foodyou.app.ui.goals.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.food.isComplete
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import com.maksimowiczm.foodyou.goals.domain.entity.RollingBudgetPreferences
import com.maksimowiczm.foodyou.goals.domain.repository.GoalsRepository
import com.maksimowiczm.foodyou.goals.domain.usecase.ObserveRollingEnergyBalanceUseCase
import com.maksimowiczm.foodyou.habits.domain.repository.CoffeeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

internal class GoalsViewModel(
    private val goalsRepository: GoalsRepository,
    private val observeDiaryMealsUseCase: ObserveDiaryMealsUseCase,
    private val observeRollingEnergyBalanceUseCase: ObserveRollingEnergyBalanceUseCase,
    private val rollingBudgetPreferencesRepository: UserPreferencesRepository<RollingBudgetPreferences>,
    private val coffeeRepository: CoffeeRepository,
) : ViewModel() {
    private val mealsFlows = mutableMapOf<LocalDate, StateFlow<GoalsScreenUiState?>>()

    fun observeUiStateByDate(date: LocalDate): StateFlow<GoalsScreenUiState?> {
        mealsFlows[date]?.let {
            return it
        }

        val meals =
            observeDiaryMealsUseCase.observe(date).map { list ->
                list.map {
                    MealModel(
                        id = it.meal.id,
                        name = it.meal.name,
                        nutritionFacts = it.nutritionFacts,
                        incompleteFoods =
                            it.entries
                                .filterNot { it.nutritionFacts.isComplete }
                                .map { it.name }
                                .distinct(),
                    )
                }
            }
        val goal = goalsRepository.observeDailyGoals(date)
        val rollingBalance = observeRollingEnergyBalanceUseCase.observe(date)
        val rollingBudgetPreferences = rollingBudgetPreferencesRepository.observe()
        val habitsCaffeineMg = coffeeRepository.observeCaffeineMgForDate(date)

        val flow =
            combine(
                    meals,
                    goal,
                    rollingBalance,
                    rollingBudgetPreferences,
                    habitsCaffeineMg,
                    ::GoalsScreenUiState,
                )
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(30_000),
                    initialValue = null,
                )

        mealsFlows[date] = flow
        return flow
    }
}
