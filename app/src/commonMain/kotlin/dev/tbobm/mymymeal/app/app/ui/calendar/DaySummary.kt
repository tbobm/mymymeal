package dev.tbobm.mymymeal.app.app.ui.calendar

import dev.tbobm.mymymeal.app.common.domain.food.NutritionFactsField
import dev.tbobm.mymymeal.app.common.domain.food.sum
import dev.tbobm.mymymeal.app.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import dev.tbobm.mymymeal.app.goals.domain.repository.GoalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.LocalDate

/** A single day's logged totals against its goal, for the month-grid coloring and day summary. */
data class DaySummary(
    val date: LocalDate,
    val energy: Double,
    val energyGoal: Double,
    val proteins: Double,
    val proteinsGoal: Double,
    val carbohydrates: Double,
    val carbohydratesGoal: Double,
    val fats: Double,
    val fatsGoal: Double,
) {
    val status: DayStatus
        get() = classifyDay(loggedEnergy = energy, goalEnergy = energyGoal)
}

/**
 * Observes [DaySummary] for a single [date]. Meal sort order (which [ObserveDiaryMealsUseCase]
 * re-ticks every second for) doesn't affect the summed totals, so [distinctUntilChanged] collapses
 * those ticks and the grid only recomposes when logged totals actually change.
 */
fun observeDaySummary(
    date: LocalDate,
    observeDiaryMeals: ObserveDiaryMealsUseCase,
    goalsRepository: GoalsRepository,
): Flow<DaySummary> =
    combine(observeDiaryMeals.observe(date), goalsRepository.observeDailyGoals(date)) { meals, goal
            ->
            val facts = meals.map { it.nutritionFacts }.sum()
            DaySummary(
                date = date,
                energy = facts.energy.value ?: 0.0,
                energyGoal = goal[NutritionFactsField.Energy],
                proteins = facts.proteins.value ?: 0.0,
                proteinsGoal = goal[NutritionFactsField.Proteins],
                carbohydrates = facts.carbohydrates.value ?: 0.0,
                carbohydratesGoal = goal[NutritionFactsField.Carbohydrates],
                fats = facts.fats.value ?: 0.0,
                fatsGoal = goal[NutritionFactsField.Fats],
            )
        }
        .distinctUntilChanged()
